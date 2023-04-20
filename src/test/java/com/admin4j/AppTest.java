package com.admin4j;


import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static void main(String[] args) throws Exception {


        File file = new File("D:\\workspace\\github\\plugin-example\\target\\plugin-example-1.0-SNAPSHOT.jar");
        URL[] urls = new URL[]{

                file.toURI().toURL()
        };
        URLClassLoader urlClassLoader = new URLClassLoader(urls);

        System.out.println("urlClassLoader = " + urlClassLoader);

        Class<?> mainClass = urlClassLoader.loadClass("com.admin4j.plugin.example.Main");
        System.out.println("aClass = " + mainClass);

        Method mainPrint = mainClass.getMethod("print");

        //Class<?> studentClass = urlClassLoader.loadClass("com.admin4j.plugin.example.Student");
        //System.out.println("studentClass = " + studentClass);
        //
        //Method setStudent = mainClass.getMethod("setStudent", studentClass);
        //  Method setName = studentClass.getMethod("setName", String.class);
        //Method setAge = studentClass.getMethod("setAge", Integer.class);
        //Object student = studentClass.newInstance();
        //setName.invoke(student, "小米");
        //setAge.invoke(student,1);

        Object main = mainClass.newInstance();


        //setStudent.invoke(main, student);
        mainPrint.invoke(main);
    }
}
