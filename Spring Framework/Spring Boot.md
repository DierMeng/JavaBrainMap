# Spring Boot

## @SpringBootApplication 注解

### @Configuration

### @EnableAutoConfiguration

### @ComponentScan

- @Component
- @Service
- @Repository
- @Controller
- @Entity

## 多 Profile 使用与切换

### Profile 是 Spring 对不同环境提供不同配置功能的支持，可以通过激活、指定参数等方式快速切换环境。

- 默认使用 application.properties、application。yml 配置文件。
- application-{profile}.properties
- application-{profile}.yml

### yml 多文档块

- yml 文件中支持使用三个短横线分割文档块的方式

### 激活指定配置方式

- 配置文件方式

	- active: dev(prod、test)
	- spring.profiles.active=dev

- 命令行方式

	- java -jarglorze.jar --spring.profiles.active=prod；

## Spring Boot Actuator

### conditions

- 显示自动配置的信息

### beans

- 显示应用程序上下文所有的 Spring bean

### configprops

- 显示所有 @ConfigurationProperties 的配置属性列表

### dump

- 显示线程活动的快照

### env

- 显示环境变量，包括系统环境变量以及应用环境变量

### health

- 显示应用程序的健康指标，值由 HealthIndicator 的实现类提供；结果有 UP、 DOWN、OUT_OF_SERVICE、UNKNOWN

### info

- 显示应用的信息

### mappings

- 显示所有的 URL 路径

### metrics

- 显示应用的度量标准信息

## Spring Boot 对 Spring Boot Starter 加载的原理

SpringBoot 在启动时会去依赖的 starter 包中寻找 /META-INF/spring.factories 文件，然后根据文件中配置的路径去扫描项目所依赖的 Jar 包

### Spring Boot 在启动时扫描项目所依赖的 JAR 包，寻找包含 spring.factories 文件的 JAR 包

### 根据 spring.factories 配置加载 AutoConfigure 类

### 根据 @Conditional 注解的条件，进行自动配置并将 Bean 注入 Spring Context

## Spring Boot 自动配置的几种方式

### @EnableXXX

- @EnableFeignClients
- @EnableDiscoveryClient

### 基于 class 是否存在的自动化配置

- @ConditionalOnClass

### 基于配置属性是否存在的配置

- @ConditionalOnProperty

## 注解

### @Conditional 注解及作用

- @ConditionalOnBean

	- 当容器中有指定的 Bean 的条件下

- @ConditionalOnClass

	- 当类路径下有指定的类的条件下

- @ConditionalOnExpression

	- 基于 SpEL 表达式作为判断条件

- @ConditionalOnJava

	- 基于 JVM 版本作为判断条件

- @ConditionalOnJndi

	- 在 JNDI 存在的条件下查找指定的位

- @ConditionalOnMissingBean

	- 当容器中没有指定 Bean 的情况下

- @ConditionalOnMissingClass

	- 当类路径下没有指定的类的条件下

- @ConditionalOnNotWebApplication

	- 当前项目不是 Web 项目的条件下

- @ConditionalOnProperty

	- 指定的属性是否有指定的值

- @ConditionalOnResource

	- 类路径下是否有指定的资源

- @ConditionalOnSingleCandidate

	- 当指定的 Bean 在容器中只有一个，或者在有多个 Bean 的情况下，用来指定首选的 Bean

- @ConditionalOnWebApplication

	- 当前项目是 Web 项目的条件下

## SpringBoot Spring IOC  BeanFactory 运行时动态注册 bean

### 义一个没有被 Spring 管理的 Controller，实现 InitializingBean 接口

### 获取 Spring 上下文

- private static ApplicationContext applicationContext;
- 通过名字获取上下文中的 bean
- 通过类型获取上下文中的bean

### 将 applicationContext 转换为 ConfigurableApplicationContext

### 获取 bean 工厂并转换为 DefaultListableBeanFactory

### 通过 BeanDefinitionBuilder 创建 bean 定义

### 设置属性

### 注册bean

- defaultListableBeanFactory.registerBeanDefinition("userController", beanDefinitionBuilder.getRawBeanDefinition());

