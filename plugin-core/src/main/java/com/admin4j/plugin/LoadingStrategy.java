package com.admin4j.plugin;


import com.admin4j.plugin.configuration.ConfigurationReader;

import java.util.Map;

/**
 * 加载策略
 *
 * @author andanyang
 * @since 2023/4/20 10:00
 */
public interface LoadingStrategy extends Comparable<LoadingStrategy> {

    /**
     * The maximum priority
     */
    int MAX_PRIORITY = Integer.MAX_VALUE;
    /**
     * The minimum priority
     */
    int MIN_PRIORITY = Integer.MIN_VALUE;
    /**
     * Normal Priority
     */
    int NORMAL_PRIORITY = 0;


    default boolean enable() {
        return true;
    }

    <T> Map<String, Class<T>> loadClass(Class<T> tClass);

    /**
     * 使用当前 ClassLoad
     *
     * @return
     */
    default boolean preferExtensionClassLoader() {
        return false;
    }

    default String[] excludedPackages() {
        return null;
    }

    /**
     * 是否允许覆盖
     *
     * @return
     */
    default boolean overridden() {
        return false;
    }

    /**
     * 排序，大的先执行
     *
     * @return 排序
     */
    default int order() {
        return NORMAL_PRIORITY;
    }

    @Override
    default int compareTo(LoadingStrategy o) {

        return o.order() > order() ? 1 : 0;
    }


    ConfigurationReader getConfigurationReader();

}
