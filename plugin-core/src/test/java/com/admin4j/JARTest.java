package com.admin4j;

import com.admin4j.plugin.api.UserService;
import com.admin4j.plugin.classloader.PluginClassLoader;
import org.junit.Test;

import java.net.JarURLConnection;
import java.net.URL;

/**
 * @author andanyang
 * @since 2023/4/21 8:39
 */
public class JARTest {


    @Test
    public void testJarLoad() throws Exception {


        //File file = new File("C:\\Users\\andanyang\\Desktop\\plugin-java\\plugin\\plugin-example-impl-two-1.0-SNAPSHOT.jar");
        //JarFile jarFile = new JarFile(file);
        URL url = new URL("jar:file:/" + "C:\\Users\\andanyang\\Desktop\\plugin-java\\plugin\\plugin-example-impl-two-1.0-SNAPSHOT.jar" + "!/");
        JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
        PluginClassLoader pcl = new PluginClassLoader("test");
        pcl.addUrls(url);

        //test close
        urlConnection.getJarFile().close();

        Class<UserService> aClass = (Class<UserService>) pcl.loadClass("com.admin4j.plugin.example.UserService10Impl");

        UserService userService = aClass.newInstance();
        String s = userService.setUserName("test");
        String userName = userService.getUserName();

        System.out.println("s = " + s);
        System.out.println("userName = " + userName);
    }
}
