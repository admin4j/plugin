package com.admin4j.plugin.loading;

import com.admin4j.plugin.PluginClassLoaderManager;

/**
 * @author andanyang
 * @since 2023/4/20 11:19
 */
public class LocalPluginLoadingStrategy extends ClassPathPluginLoadingStrategy {

    /**
     * 插件路径
     */
    private static final String PLUGIN_PATH = System.getProperty("PLUGIN_PATH", "./plugin");


    @Override
    public int order() {
        return super.order() - 100;
    }


    @Override
    public ClassLoader findClassLoader() {

        return PluginClassLoaderManager.SHARE_INSTANCE.addPluginClassLoader("LOCAL", PLUGIN_PATH);
    }
}
