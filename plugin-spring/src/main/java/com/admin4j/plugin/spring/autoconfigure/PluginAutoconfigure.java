package com.admin4j.plugin.spring.autoconfigure;

import com.admin4j.plugin.LoadingStrategy;
import com.admin4j.plugin.configuration.reader.XmlConfigurationReader;
import com.admin4j.plugin.spring.remote.RemoteJarService;
import com.admin4j.plugin.spring.remote.RemotePluginLoadingStrategy;
import com.admin4j.plugin.spring.service.PluginSelectService;
import com.admin4j.plugin.spring.service.impl.DefaultPluginSelectServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * @author andanyang
 * @since 2023/4/24 9:40
 */
@Order(1)
@AutoConfigureBefore
public class PluginAutoconfigure {

    @Bean
    @ConditionalOnBean(RemoteJarService.class)
    @Order(1)
    public LoadingStrategy remotePluginLoadingStrategy(RemoteJarService remoteJarService) {
        return new RemotePluginLoadingStrategy(new XmlConfigurationReader(), remoteJarService);
    }


    @Bean
    @ConditionalOnMissingBean(PluginSelectService.class)
    public PluginSelectService pluginSelectService() {
        return new DefaultPluginSelectServiceImpl();
    }
}
