package com.admin4j.plugin;

/**
 * @author andanyang
 * @since 2023/4/20 10:51
 */
public @interface Activate {
    String[] group() default {};

    String[] value() default {};
    int order() default 0;
}
