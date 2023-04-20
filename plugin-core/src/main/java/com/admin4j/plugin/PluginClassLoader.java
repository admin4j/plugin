package com.admin4j.plugin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author andanyang
 * @since 2023/4/20 14:43
 */
public class PluginClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(PluginClassLoader.class);
    private final List<JarURLConnection> cacheJarFiles = new ArrayList<>();
    private String name;

    public PluginClassLoader(String name) {
        this(new URL[]{}, PluginClassLoader.class.getClassLoader());
        this.name = name;
    }

    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        logger.debug("PluginClassLoader parent is {}", parent);
        System.out.println("PluginClassLoader parent is = " + parent);
    }

    public PluginClassLoader(URL[] urls) {
        super(urls, PluginClassLoader.class.getClassLoader());
    }

    public List<JarURLConnection> getCacheJarFiles() {
        return cacheJarFiles;
    }

    public String getName() {
        return name;
    }

    public void addUrlFile(String... paths) {
        URL[] urls = Stream.of(paths).map(path -> new File(path))
                .flatMap(file -> {
                    if (file.isDirectory()) {
                        try {
                            return Files.walk(Paths.get(file.getPath())).map(path -> path.toFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    } else {
                        return Stream.of(file);
                    }

                }).filter(file -> file.getName().endsWith(".jar")).map(file -> {
                    try {
                        return new URL("jar:file:/" + file.getAbsolutePath() + "!/");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).toArray(URL[]::new);

        addUrlFile(urls);
    }

    public void addUrlFile(URL... urls) {
        for (URL url : urls) {
            try {
                URLConnection uc = url.openConnection();
                if (uc instanceof JarURLConnection) {
                    uc.setUseCaches(true);
                    JarURLConnection juc = (JarURLConnection) uc;
                    juc.getManifest();
                    cacheJarFiles.add(juc);
                    addURL(url);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void unloadJarFile() {
        for (JarURLConnection juc : cacheJarFiles) {
            try {
                juc.getJarFile().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cacheJarFiles.clear();
    }

}
