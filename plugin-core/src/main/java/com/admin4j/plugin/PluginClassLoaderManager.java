package com.admin4j.plugin;


import java.io.IOException;
import java.util.HashMap;

/**
 * @author andanyang
 * @since 2023/4/20 14:59
 */
public class PluginClassLoaderManager {

    public static PluginClassLoaderManager SHARE_INSTANCE = new PluginClassLoaderManager();
    private final HashMap<String, PluginClassLoader> pluginMap = new HashMap<>();

    private PluginClassLoaderManager() {

    }


    public PluginClassLoader addPluginClassLoader(String loadName, String... paths) {
        removePluginClassLoader(loadName);
        PluginClassLoader pcl = new PluginClassLoader(loadName);
        pcl.addUrlFile(paths);
        pluginMap.put(loadName, pcl);

        return pcl;
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
