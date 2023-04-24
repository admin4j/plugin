package com.admin4j.plugin.spring.remote;

import com.admin4j.common.util.SpringUtils;
import com.admin4j.plugin.PluginClassLoaderManager;
import com.admin4j.plugin.classloader.PluginClassLoader;
import com.admin4j.plugin.loading.LocalPluginLoadingStrategy;

import java.io.IOException;
import java.util.List;

/**
 * jar 放在 OSS 上
 *
 * @author andanyang
 * @since 2023/4/24 16:22
 */
public class RemotePluginLoadingStrategy extends LocalPluginLoadingStrategy {


    private RemoteJarService remoteJarService;

    @Override
    public boolean enable() {
        
        return true;
    }

    @Override
    public int order() {
        return super.order() - 10;
    }

    @Override
    public ClassLoader findClassLoader() {

        try {
            PluginClassLoader remote = PluginClassLoaderManager.SHARE_INSTANCE.getPluginClassLoader("REMOTE");

            RemoteJarService bean = SpringUtils.getBean(RemoteJarService.class);
            this.remoteJarService = bean;

            //遍历远程文件
            List<String> allJarUrl = remoteJarService.getAllJarUrl();
            for (String url : allJarUrl) {
                remote.addHttpUrl(url);
            }

            return remote;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
