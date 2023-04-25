package com.admin4j.plugin.spring.factory;

import com.admin4j.api.anno.SPI;
import com.admin4j.plugin.ExtensionLoader;
import com.admin4j.plugin.spring.service.PluginSelectService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * @author andanyang
 * @since 2023/4/21 14:20
 */
@Slf4j
@Data
//@RequiredArgsConstructor
public class AdaptiveFactoryBean<T> implements FactoryBean<T> {

    private Class<T> mapperInterface;
    private PluginSelectService pluginSelectService;


    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(AdaptiveFactoryBean.class.getClassLoader(), new Class[]{mapperInterface}, (proxy, method, args) -> {

            String pluginName = pluginSelectService.getPluginName(mapperInterface);
            if (StringUtils.isBlank(pluginName)) {
                SPI annotation = mapperInterface.getAnnotation(SPI.class);
                pluginName = annotation.value();
            }
            if (StringUtils.isBlank(pluginName)) {
                return null;
            }
            log.debug("Interface {} selected {}", mapperInterface, pluginName);
            T extension = ExtensionLoader.getExtensionLoader(mapperInterface).getExtension(pluginName);
            //System.out.println("前置拦截");
            Object invoke = method.invoke(extension, args);
            //System.out.println("后置拦截");
            return invoke;
        });
    }


    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
