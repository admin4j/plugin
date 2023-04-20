package com.admin4j.plugin.anno;

import java.lang.annotation.*;

/**
 * @author andanyang
 * @since 2023/4/20 9:33
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * default extension name
     */
    String value() default "";
}
