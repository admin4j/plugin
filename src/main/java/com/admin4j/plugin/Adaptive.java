package com.admin4j.plugin;

/**
 *
 * 自适应包
 * @author andanyang
 * @since 2023/4/20 10:41
 */
public @interface Adaptive {

    String[] value() default {};
}
