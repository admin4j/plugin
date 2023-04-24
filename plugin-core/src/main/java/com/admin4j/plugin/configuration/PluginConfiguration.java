package com.admin4j.plugin.configuration;

import lombok.Data;

/**
 * @author andanyang
 * @since 2023/4/24 16:15
 */
@Data
public class PluginConfiguration {

    /**
     * 插件名称
     */
    private String name;
    private String version;
    private String description;
    private String interfaceName;
    private String implementName;
}
