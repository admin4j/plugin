# 概述

前置项目中，经常有定制的需求，如何更优雅实现定制的需求

前置云端拆分第一阶段，是把前置云端的代码拆分，第二阶段的目标是实现前置云端共用一个 SDK，如何实现扩展性成了一个问题，我试着给出自己的思考

设计模式
====

对于一些有可能发生变化的地方，进行封装（关注点分离），应用策略模式、责任链模式等手段进行扩展点设计。

![](https://mmbiz.qpic.cn/mmbiz_jpg/gInEnAhFf5wu59mrQx0ucec7S8DaC8AcHhXjxZVOtoiaQDpw2VxBrsWlPAzzP7DL8XwwbLP2M7mlx7OKiauAfBNg/640?wx_fmt=jpeg)

扩展点
===

Java 原生 SPI
-----------

JAVA SPI = 基于接口的编程＋策略模式＋配置文件 的动态加载机制

对于 Java 中的 SPI，有如下的约定：

1.  在 META-INF/services / 目录中创建以接口全限定名命名的文件该文件内容为 Api 具体实现类的全限定名

2.  使用 ServiceLoader 类动态加载 META-INF 中的实现类

3.  如 SPI 的实现类为 Jar 则需要放在主程序 classPath 中

4.  Api 具体实现类必须有一个不带参数的构造方法

实际上就是 JDK 的就是 ServiceLoader 会遍历所有 jar 查找 META-INF/services / 接口类名 文件，找到后就加载文件中配置的文件进行实例化使用。

Spring 的扩展点
-----------

SPI 实现机制

在 Spring 中提供了 SPI 机制，我们只需要在 META-INF/spring.factories 中配置接口实现类名，即可通过服务发现机制，在运行时加载接口的实现类：

```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration
```

配置好 spring.factories 文件后，我们就可以通过 SpringFactoriesLoader 动态加载接口实现类了

BeanFactoryPostProcessor 接口

BeanFactory 生成后，如果想对 BeanFactory 进行一些处理，该怎么办呢？BeanFactoryPostProcessor 接口就是用来处理 BeanFactory 的。

BeanDefinitionRegistryPostProcessor 接口

在 Spring 容器初始化时，首先会读取应用程序中的配置文件，并解析出所有的 Bean 定义，然后将这些 Bean 定义注册到容器中。在这个过程中，BeanDefinitionRegistryProcessor 提供了一种机制，允许开发人员在 Bean 定义注册之前和之后对 Bean 定义进行自定义处理，例如添加，修改或删除 Bean 定义等。

Mybatis 中 org.mybatis.spring.mapper.MapperScannerConfigurer 就实现了该方法，在只有接口没有实现类的情况下找到接口方法与 sql 之间的联系从而生成 BeanDefinition 并注册。

BeanPostProcessor 接口

在 Spring 容器实例化 bean 之后，在执行 bean 的初始化方法前后，允许我们自定义修改新的 bean 实例，如修改 bean 的属性，可以给 bean 生成一个动态代理实例等等。

Spring AOP 的底层处理也是通过实现 BeanPostProcessor 来执行代理包装逻辑的。

AOP
===

AOP 为 Aspect Oriented Programming 的缩写，意为：面向切面编程，通过预编译方式和运行动态代理实现程序功能的统一维护的一种技术。AOP 是 OOP 的延续，是软件开发中的一个热点，也是 Spring 框架中的一个重要内容，是函数式编程的一种衍生范型。利用 AOP 可以对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的耦合度降低，提高程序的可重用性，同时提高开发的效率。

我们一般做活动的时候，一般对每一个接口都会做活动的有效性校验（是否开始、是否结束等等）、以及这个接口是不是需要用户登录。

![](https://mmbiz.qpic.cn/mmbiz_png/gInEnAhFf5wu59mrQx0ucec7S8DaC8AcJyCVLkYUmyX9Vza8d2ymHbLiaZM0neUKIazFTOyNCIA5DjPOHZwdyMw/640?wx_fmt=png)

![](https://mmbiz.qpic.cn/mmbiz_png/gInEnAhFf5wu59mrQx0ucec7S8DaC8AcqywsXeXnXkmff9BL1BTNmBM0rBXv3RnKIqG4XVCNGm2CmPPrM40TMg/640?wx_fmt=png)

![](https://mmbiz.qpic.cn/mmbiz_png/gInEnAhFf5wu59mrQx0ucec7S8DaC8AcByILWKMZImDPwIOmbibz5vrL5fLkMcsclics191anWSIAesyORwuLWOg/640?wx_fmt=png)

AOP + 设计模式

可配置化

插件化
===

常见的动态插件的实现方式有 SPI、OSGI 等方案，插件化开发模式正在很多编程语言或技术框架中得以广泛的应用实践，比如大家熟悉的 jenkins，docker 可视化管理平台 rancher，以及日常编码使用的编辑器 idea，eclipse 等，随处可见的带有热插拔功能的插件，让系统像插了翅膀一样，大大提升了系统的扩展性和伸缩性，也拓展了系统整体的使用价值。

#### 1 模块解耦

实现服务模块之间解耦的方式有很多，但是插件来说，其解耦的程度似乎更高，而且更灵活，可定制化、个性化更好。

#### 2 提升扩展性和开放性

插件化机制让系统的扩展性得以提升，从而可以丰富系统的周边应用生态。

#### 3 方便第三方接入

有了插件之后，第三方应用或系统如果要对接自身的系统，直接基于系统预留的插件接口完成一套适合自己业务的实现即可，而且对自身系统的侵入性很小，甚至可以实现基于配置参数的热加载，方便灵活，开箱即用。

![](https://mmbiz.qpic.cn/mmbiz_png/gInEnAhFf5wu59mrQx0ucec7S8DaC8Ac7ic9zw6RibQfSLSO4QwNImSD6CWplMNOXfnVD0Opj3QUqaNuvknnE6icw/640?wx_fmt=png)

**最佳实践**

1.  面向接口编程

2.  方法粒度足够细（对应一个功能点），方法异常能够区分
