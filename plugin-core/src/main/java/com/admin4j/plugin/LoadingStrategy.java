package com.admin4j.plugin;


import java.util.Map;

/**
 * 加载策略
 *
 * @author andanyang
 * @since 2023/4/20 10:00
 */
public interface LoadingStrategy extends Comparable {

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

    <T> Map<String, Class<T>> loadClass(Class<T> tClass);

    String directory();

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
    default int compareTo(Object o) {

        if (o instanceof LoadingStrategy) {
            return ((LoadingStrategy) o).order() > order() ? 1 : 0;
        }
        return 1;
    }

    /**
     * 类加载器
     *
     * @return
     */
    default ClassLoader findClassLoader() {

        ClassLoader cl = null;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = LoadingStrategy.class.getClassLoader();
        }

        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        return cl;
    }
}
