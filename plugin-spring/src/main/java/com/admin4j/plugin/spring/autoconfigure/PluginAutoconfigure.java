package com.admin4j.plugin.spring.autoconfigure;

import com.admin4j.plugin.spring.service.PluginSelectService;
import com.admin4j.plugin.spring.service.impl.DefaultPluginSelectServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author andanyang
 * @since 2023/4/24 9:40
 */
public class PluginAutoconfigure {


    @Bean
    @ConditionalOnMissingBean(PluginSelectService.class)
    public PluginSelectService pluginSelectService() {
        return new DefaultPluginSelectServiceImpl();
    }
}
