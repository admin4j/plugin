package com.admin4j.plugin.loading;

import com.admin4j.plugin.LoadingStrategy;
import com.admin4j.plugin.PluginClassLoaderManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author andanyang
 * @since 2023/4/20 11:19
 */
public class LocalPluginLoadingStrategy implements LoadingStrategy {


    private static final Logger logger = LoggerFactory.getLogger(LocalPluginLoadingStrategy.class);

    private static final String PLUGIN_PATH = "./plugin";

    @Override
    public String directory() {
        return "META-INF/plugin/";
    }

    @Override
    public boolean overridden() {
        return true;
    }

    @Override
    public int order() {
        return LoadingStrategy.MAX_PRIORITY - 100;
    }


    @Override
    public <T> Map<String, Class<T>> loadClass(Class<T> tClass) {

        PluginClassLoaderManager pluginClassLoaderManager = new PluginClassLoaderManager();
        ClassLoader classLoader = pluginClassLoaderManager.addPluginClassLoader("LOCAL", PLUGIN_PATH);

        //name -> Class
        Map<String, Class<T>> extensionClasses = new HashMap<>();

        String fileName = directory() + tClass.getName();

        try {

            Enumeration<URL> urls = null;

            if (preferExtensionClassLoader()) {

                ClassLoader extensionLoaderClassLoader = LocalPluginLoadingStrategy.class.getClassLoader();
                if (ClassLoader.getSystemClassLoader() != extensionLoaderClassLoader) {
                    urls = extensionLoaderClassLoader.getResources(fileName);
                }
            }


            if (urls == null || !urls.hasMoreElements()) {
                if (classLoader != null) {
                    urls = classLoader.getResources(fileName);
                } else {
                    urls = ClassLoader.getSystemResources(fileName);
                }
            }

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceURL = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceURL, tClass);
                }
            }

        } catch (Exception e) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    tClass.getName() + ", description file: " + fileName + ").", e);
        }

        return extensionClasses;
    }

    private <T> void loadResource(Map<String, Class<T>> extensionClasses,
                                  ClassLoader classLoader,
                                  URL resourceURL,
                                  Class<T> type) {

        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
                String line;
                String clazz = null;
                while ((line = reader.readLine()) != null) {
                    final int ci = line.indexOf('#');
                    if (ci >= 0) {
                        line = line.substring(0, ci);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            String name = null;
                            int i = line.indexOf('=');
                            if (i > 0) {
                                name = line.substring(0, i).trim();
                                clazz = line.substring(i + 1).trim();
                            } else {
                                clazz = line;
                            }
                            if (StringUtils.isNotEmpty(clazz) && !isExcluded(clazz, excludedPackages())) {

                                Class<?> aClass = Class.forName(clazz, true, classLoader);

                                if (!type.isAssignableFrom(aClass)) {
                                    throw new IllegalStateException("Error occurred when loading extension class (interface: " +
                                            type + ", class line: " + aClass.getName() + "), class "
                                            + aClass.getName() + " is not subtype of interface.");
                                }


                                //loadClass(extensionClasses, resourceURL, aClass, name, overridden());

                                extensionClasses.put(name, (Class<T>) aClass);
                            }
                        } catch (Throwable t) {
                            IllegalStateException e = new IllegalStateException(
                                    "Failed to load extension class (interface: " + type + ", class line: " + line + ") in " + resourceURL +
                                            ", cause: " + t.getMessage(), t);
                            //exceptions.put(line, e);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }


    protected boolean isExcluded(String className, String... excludedPackages) {
        if (excludedPackages != null) {
            for (String excludePackage : excludedPackages) {
                if (className.startsWith(excludePackage + ".")) {
                    return true;
                }
            }
        }
        return false;
    }

}
