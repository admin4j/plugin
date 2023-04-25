package com.admin4j.api;

/**
 * @author andanyang
 * @since 2023/4/24 15:31
 */
public interface ProviderPlugin<T> {

    boolean supports(T t);
}
