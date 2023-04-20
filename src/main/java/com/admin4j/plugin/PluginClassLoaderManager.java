package com.admin4j.plugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * @author andanyang
 * @since 2023/4/20 14:59
 */
public class PluginClassLoaderManager {

    private final HashMap<String, PluginClassLoader> pluginMap = new HashMap<>();

    public PluginClassLoader addPluginClassLoader(String loadName, String... paths) {
        removePluginClassLoader(loadName);
        PluginClassLoader pcl = new PluginClassLoader(loadName);
        pcl.addUrlFile(paths);
        pluginMap.put(loadName, pcl);

        return pcl;
    }

    public Class getClass(String loadName, String className) {
        PluginClassLoader pcl = pluginMap.get(loadName);
        Class clazz = null;
        if (pcl != null) {
            try {
                clazz = pcl.loadClass(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return clazz;
    }

    public <S> S getNewObject(String loadName, String className, Class<S> clazzPresen) {
        Class clazz = getClass(loadName, className);
        if (clazzPresen.isAnnotationPresent(clazz)) {
            try {
                return (S) clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException
                     | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public void removePluginClassLoader(String loadName) {
        PluginClassLoader pcl = pluginMap.remove(loadName);
        if (pcl != null) {
            pcl.unloadJarFile();
            try {
                pcl.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
