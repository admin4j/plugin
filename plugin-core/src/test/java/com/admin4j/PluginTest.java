package com.admin4j;

import com.admin4j.order.OrderService;
import com.admin4j.plugin.ExtensionLoader;
import org.junit.Test;

/**
 * @author andanyang
 * @since 2023/4/20 11:27
 */
public class PluginTest {

    @Test
    public void testPlugin() {

        OrderService def = ExtensionLoader.getExtensionLoader(OrderService.class).getExtension("def");
        def.createOrder("sad");
        def = ExtensionLoader.getExtensionLoader(OrderService.class).getExtension("def2");
        def.createOrder("sad2");
        def = ExtensionLoader.getExtensionLoader(OrderService.class).getExtension("def3");
        def.createOrder("sad3");


        def = ExtensionLoader.getExtensionLoader(OrderService.class).getDefaultExtension();
        String sad34 = def.createOrder("sad34");
        System.out.println("sad34 = " + sad34);
    }

}
