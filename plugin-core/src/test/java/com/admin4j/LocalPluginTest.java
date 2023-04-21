package com.admin4j;

import com.admin4j.plugin.ExtensionLoader;
import com.admin4j.plugin.PluginClassLoader;
import com.admin4j.plugin.PluginClassLoaderManager;
import com.admin4j.plugin.api.UserService;
import org.junit.Test;

import java.io.IOException;

/**
 * @author andanyang
 * @since 2023/4/20 11:27
 */
public class LocalPluginTest {

    int i = 0;

    @Test
    public void testPlugin() throws IOException {

        System.setProperty("PLUGIN_PATH", "C:\\Users\\andanyang\\Desktop\\plugin-java\\plugin");
        UserService userService;

        userService = ExtensionLoader.getExtensionLoader(UserService.class).getExtension("def");
        testBean(userService);

        userService = ExtensionLoader.getExtensionLoader(UserService.class).getExtension("def2");
        testBean(userService);

        userService = ExtensionLoader.getExtensionLoader(UserService.class).getExtension("def3");
        testBean(userService);

        PluginClassLoader local = PluginClassLoaderManager.SHARE_INSTANCE.getPluginClassLoader("LOCAL");
        local.close();
        
        userService = ExtensionLoader.getExtensionLoader(UserService.class).getDefaultExtension();
        testBean(userService);
    }

    private void testBean(UserService def) {

        i++;
        String s = def.setUserName("小米" + i);
        String userName = def.getUserName();

        System.out.println("setUserName = " + s);
        System.out.println("userName = " + userName);

    }
}
