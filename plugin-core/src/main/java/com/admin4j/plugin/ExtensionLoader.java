package com.admin4j.plugin;

import com.admin4j.plugin.anno.SPI;
import com.admin4j.plugin.context.Lifecycle;
import com.admin4j.plugin.util.Holder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;


/**
 * @author andanyang
 * @since 2023/4/20 9:34
 */
public class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");
    //保存了扩展接口和 ExtensionLoader 之间的映射关系，一个扩展接口对应一个 ExtensionLoader。key 为接口，value 为 ExtensionLoader。
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>(64);
    /**
     * 加载插件策略
     */
    private static final LoadingStrategy[] strategies = loadLoadingStrategies();
    //当前 ExtensionLoader 绑定的扩展接口类型。
    private final Class<?> type;
    //private final ExtensionFactory objectFactory;

    //存储了扩展实现类类名与实现类的实例对象之间的关系，key 为扩展实现名，value 为实例对象。
    private final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>(64);
    //存储了扩展实现类和扩展名的关系，key 为扩展实现类，value 为对应扩展名。
    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>(64);
    //存储了扩展名和扩展实现类之间的关系。key 为扩展名，value 为扩展实现类
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();
    //扩展名和扩展实现对象
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private final Map<String, Object> cachedActivates = new ConcurrentHashMap<>();
    /**
     * Record all unacceptable exceptions when using SPI
     */
    private final Set<String> unacceptableExceptions = new ConcurrentSkipListSet<>();
    private final Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();
    private final Class<?> cachedAdaptiveClass = null;
    //@SPI 注解配置的默认扩展名。
    private String cachedDefaultName;
    private Set<Class<?>> cachedWrapperClasses;

    private ExtensionLoader(Class<?> type) {

        this.type = type;
    }

    //public static void setLoadingStrategies(LoadingStrategy... strategies) {
    //    if (ArrayUtils.isNotEmpty(strategies)) {
    //        ExtensionLoader.strategies = strategies;
    //    }
    //}

    private static LoadingStrategy[] loadLoadingStrategies() {
        return StreamSupport.stream(ServiceLoader.load(LoadingStrategy.class).spliterator(), false)
                .sorted()
                .toArray(LoadingStrategy[]::new);
    }

    /**
     * 是否包含  `@SPI`注解在接口上
     */
    private static <T> boolean withExtensionAnnotation(Class<T> type) {

        return type.isAnnotationPresent(SPI.class);
    }

    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        // 必须传入类型
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        // 必须是接口类型
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
        }
        // 必须包含SPI的注解
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type (" + type +
                    ") is not an extension, because it is NOT annotated with @" + SPI.class.getSimpleName() + "!");
        }

        // 尝试从已经加载过的数据中去读取(缓存功能)
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            // 如果没有的话，才会进行初始化，并且放入到缓存汇总
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }


    /**
     * 根据所给的插件名称，获取真正的实现类
     *
     * @param name
     * @return 真正的实现类
     */
    public T getExtension(String name) {
        return getExtension(name, true);
    }

    public T getExtension(String name, boolean wrap) {

        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        if ("true".equals(name)) {
            return getDefaultExtension();
        }

        final Holder<Object> holder = getOrCreateHolder(name);
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name, wrap);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    public T getDefaultExtension() {
        getExtensionClasses();
        if (StringUtils.isBlank(cachedDefaultName) || "true".equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    private Holder<Object> getOrCreateHolder(String name) {
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        return holder;
    }

    /**
     * 创建 Extension
     */
    private T createExtension(String name, boolean wrap) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null || unacceptableExceptions.contains(name)) {
            throw findException(name);
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.getDeclaredConstructor().newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            //injectExtension(instance);


            if (wrap) {

                List<Class<?>> wrapperClassesList = new ArrayList<>();
                if (cachedWrapperClasses != null) {
                    wrapperClassesList.addAll(cachedWrapperClasses);
                    //wrapperClassesList.sort(WrapperComparator.COMPARATOR);
                    Collections.reverse(wrapperClassesList);
                }

                if (!wrapperClassesList.isEmpty()) {
                    for (Class<?> wrapperClass : wrapperClassesList) {
                        Wrapper wrapper = wrapperClass.getAnnotation(Wrapper.class);
                        if (wrapper == null
                                || (ArrayUtils.contains(wrapper.matches(), name) && !ArrayUtils.contains(wrapper.mismatches(), name))) {
                            instance = (T) wrapperClass.getConstructor(type).newInstance(instance);
                        }
                    }
                }
            }

            initExtension(instance);
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance (name: " + name + ", class: " +
                    type + ") couldn't be instantiated: " + t.getMessage(), t);
        }
    }


    /**
     * 获取所有的实现类
     *
     * @return
     */
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 同步加载扩展类
     *
     * @return 加载扩展类
     */
    private Map<String, Class<?>> loadExtensionClasses() {
        cacheDefaultExtensionName();

        Map<String, Class<?>> extensionClasses = new HashMap<>();

        for (LoadingStrategy strategy : strategies) {

            Map<String, ? extends Class<?>> stringClassMap = strategy.loadClass(type);
            extensionClasses.putAll(stringClassMap);

            for (String name : stringClassMap.keySet()) {
                String[] names = NAME_SEPARATOR.split(name);

                Class<?> aClass = stringClassMap.get(name);

                if (ArrayUtils.isNotEmpty(names)) {
                    //cacheActivateClass(clazz, names[0]);
                    for (String n : names) {
                        cacheName(aClass, n);
                        saveInExtensionClass(extensionClasses, aClass, n, strategy.overridden());
                    }
                }
            }


        }

        return extensionClasses;
    }

    /**
     * 设置默认扩展
     */
    private void cacheDefaultExtensionName() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation == null) {
            return;
        }

        String value = defaultAnnotation.value();
        if ((value = value.trim()).length() > 0) {
            String[] names = NAME_SEPARATOR.split(value);
            if (names.length > 1) {
                throw new IllegalStateException("More than 1 default extension name on extension " + type.getName()
                        + ": " + Arrays.toString(names));
            }
            if (names.length == 1) {
                cachedDefaultName = names[0];
            }
        }
    }

    //
    ///**
    // * cache Adaptive class which is annotated with <code>Adaptive</code>
    // */
    //private void cacheAdaptiveClass(Class<?> clazz, boolean overridden) {
    //    if (cachedAdaptiveClass == null || overridden) {
    //        cachedAdaptiveClass = clazz;
    //    } else if (!cachedAdaptiveClass.equals(clazz)) {
    //        throw new IllegalStateException("More than 1 adaptive class found: "
    //                + cachedAdaptiveClass.getName()
    //                + ", " + clazz.getName());
    //    }
    //}
    //
    //private void cacheWrapperClass(Class<?> clazz) {
    //    if (cachedWrapperClasses == null) {
    //        cachedWrapperClasses = new ConcurrentSkipListSet<>();
    //    }
    //    cachedWrapperClasses.add(clazz);
    //}

    ///**
    // * cache Activate class which is annotated with <code>Activate</code>
    // * <p>
    // * for compatibility, also cache class with old alibaba Activate annotation
    // */
    //private void cacheActivateClass(Class<?> clazz, String name) {
    //    Activate activate = clazz.getAnnotation(Activate.class);
    //    if (activate != null) {
    //        cachedActivates.put(name, activate);
    //    }
    //}
    //
    //private boolean isWrapperClass(Class<?> clazz) {
    //    try {
    //        clazz.getConstructor(type);
    //        return true;
    //    } catch (NoSuchMethodException e) {
    //        return false;
    //    }
    //}

    private void cacheName(Class<?> clazz, String name) {
        if (!cachedNames.containsKey(clazz)) {
            cachedNames.put(clazz, name);
        }
    }

    /**
     * put clazz in extensionClasses
     */
    private void saveInExtensionClass(Map<String, Class<?>> extensionClasses, Class<?> clazz, String name, boolean overridden) {
        Class<?> c = extensionClasses.get(name);
        if (c == null || overridden) {
            extensionClasses.put(name, clazz);
        } else if (c != clazz) {
            // duplicate implementation is unacceptable
            unacceptableExceptions.add(name);
            String duplicateMsg =
                    "Duplicate extension " + type.getName() + " name " + name + " on " + c.getName() + " and " + clazz.getName();
            logger.error(duplicateMsg);
            throw new IllegalStateException(duplicateMsg);
        }
    }


    private IllegalStateException findException(String name) {
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);

        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(name.toLowerCase())) {
                if (i == 1) {
                    buf.append(", possible causes: ");
                }
                buf.append("\r\n(");
                buf.append(i++);
                buf.append(") ");
                buf.append(entry.getKey());
                buf.append(":\r\n");
                buf.append(entry.getValue().toString());
            }
        }

        if (i == 1) {
            buf.append(", no related exception was found, please check whether related SPI module is missing.");
        }
        return new IllegalStateException(buf.toString());
    }

    private void initExtension(T instance) {
        if (instance instanceof Lifecycle) {
            Lifecycle lifecycle = (Lifecycle) instance;
            lifecycle.initialize();
        }
    }
}
