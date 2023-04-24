package com.admin4j.plugin.spring;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author andanyang
 * @since 2023/4/24 10:51
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPIMapperScan {

    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Class<? extends Annotation> annotationClass() default Annotation.class;
}
