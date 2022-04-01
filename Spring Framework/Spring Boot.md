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

## https://pic.imgdb.cn/item/62469d3327f86abb2a4453fa.png
Spring Boot 事件模型

### SpringApplicationEvent

- 和 SpringApplication 生命周期有关的所有事件的父类，继承自 Spring Framwork 的ApplicationEvent，确保了事件和应用实体 SpringApplication 产生关联

### ApplicationStartingEvent

- 开始启动中（SpringApplication 本实例实例化、本实例属性赋值、日志系统实例化）

	- SpringApplication 实例已实例化，new SpringApplication(primarySources)

		- 推断出应用类型 webApplicationType、main 方法所在类
		- 给字段 initializers 赋值：拿到 SPI 方式配置的 ApplicationContextInitializer 上下文初始化器

			- SPI(Service provider interface)，服务提供发现接口，JDK 内置的一种服务提供发现机制。一种动态替换发现的机制， 比如有个接口，想运行时动态的给它添加实现，你只需要添加一个实现。
			- 当服务的提供者提供了一种接口的实现之后，需要在 classpath 下的 META-INF/services/ 目录里创建一个以服务接口命名的文件，这个文件里的内容就是这个接口的具体的实现类。当其他的程序需要这个服务的时候，就可以通过查找这个 jar 包（一般都是以jar包做依赖）的 META-INF/services/ 中的配置文件，配置文件中有接口的具体实现类名，可以根据这个类名进行加载实例化，就可以使用该服务了。

		- 给字段 listeners 赋值：拿到 SPI 方式配置的 ApplicationListener 应用监听器
		- 发送 ApplicationStartingEvent 事件

			- LoggingApplicationListener

				- 对日志系统抽象 LoggingSystem 执行实例化以及初始化之前的操作，默认 Logback

			- BackgroundPreinitializer

				- 启动一个后台进行对一些类进行预热

					- ValidationInitializer
					- JacksonInitializer

			- DelegatingApplicationListener

				- 监听 ApplicationEvent，实际上只有 ApplicationEnvironmentPreparedEvent 到达时生效

			- LiquibaseServiceLocatorApplicationListener

### ApplicationEnvironmentPreparedEvent

- 环境已准备好，Spring Boot 的环境抽象 Enviroment 已经准备完毕，但此时其上下文 ApplicationContext 还没有创建

	- 封装命令行参数（main 方法的 args）到 ApplicationArguments 里面
	- 创建出一个环境抽象实例 ConfigurableEnvironment 的实现类，并且填入 Profiles 配置和 Properties 属性
	- 发送 ApplicationEnvironmentPreparedEvent 事件

		- 对环境抽象 Enviroment 的填值，均是由监听此事件的监听器去完成
		- BootstrapApplicationListener

			- 优先级最高，用于启动/创建 Spring Cloud 的应用上下文，此时 Spring Boot 的上下文 ApplicationContext 还并没有创建，类似于 Bean 的初始化，初始化 A 的时候遇到 B，需要先完成 B 的初始化

		- LoggingSystemShutdownListener

			- 对 LogbackLoggingSystem 先清理，再重新初始化一次。

		- ConfigFileApplicationListener

			- 加载 SPI 配置的所有的 EnvironmentPostProcessor 实例，并且排好序。
			- 排好序后，分别一个个的执行 EnvironmentPostProcessor

				- SystemEnvironmentPropertySourceEnvironmentPostProcessor
				- SpringApplicationJsonEnvironmentPostProcessor

					- 把环境中 spring.application.json=xxx 值解析成为一个 MapPropertySource 属性源，然后放进环境里面去

				- CloudFoundryVcapEnvironmentPostProcessor
				- ConfigFileApplicationListener

					- 加载 application.properties/yaml 等外部化配置，解析好后放进环境里

						- 外部化配置默认的优先级

							- classpath:/
							- classpath:/config/
							- file:./
							- file:./config/
							- 当前工程下的 config 目录里的 application.properties 优先级最高，当前工程类路径下的 application.properties 优先级最低

						- bootstrap.xxx 也是由它负责加载的，处理规则一样

				- DebugAgentEnvironmentPostProcessor

		- AnsiOutputApplicationListener

			- 终端（可以是控制台、可以是日志文件）支持Ansi彩色输出，使其更具可读性。

		- LoggingApplicationListener

			- 根据 Enviroment 环境完成 initialize() 初始化动作：日志等级、日志格式模版等

		- ClasspathLoggingApplicationListener

			- 用于把 classpath 路径以 log.debug() 输出

		- BackgroundPreinitializer

			- 本事件达到时无动作

		- DelegatingApplicationListener

			- 执行通过外部化配置 context.listener.classes = xxx,xxx 的监听器们，然后把该事件广播给关心此事件的监听器执行

		- FileEncodingApplicationListener

			- 检测当前系统环境的 file.encoding 和 spring.mandatory-file-encoding 设置的值是否一样，不一样则抛出异常
			- 如果不配置 spring.mandatory-file-encoding 则不检查

	- bindToSpringApplication(environment)

		- 把环境属性中 spring.main.xxx = xxx 绑定到当前的 SpringApplication 实例属性上

### ApplicationContextInitializedEvent，也叫 contextPrepared 事件 

- 上下文已实例化，完成应用上下文 ApplicationContext 的准备工作，并且执行所有注册的上下文初始化器 ApplicationContextInitializer。但是此时，单例 Bean 是仍旧还没有初始化，并且 WebServer 也还没有启动

	- printBanner

		- 打印 Banner 图，默认打印的是 Spring Boot 字样

	- 根据是否是 web 环境、是否是 REACTIVE 等，用空构造器创建出一个 ConfigurableApplicationContext 上下文实例

		- SERVLET

			- AnnotationConfigServletWebServerApplicationContext

		- REACTIVE

			- AnnotationConfigReactiveWebServerApplicationContext

		- 非 web 环境

			- AnnotationConfigApplicationContext，Spring Cloud 使用

	- 上下文参数设置

		- 环境 Enviroment 环境设置给它
		- 设置 beanNameGenerator、resourceLoader、ConversionService 等组件
		- 实例化所有的 ApplicationContextInitializer 上下文初始化器，并且排序好后挨个执行它

			- BootstrapApplicationListener.AncestorInitializer

				- 把 Spring Cloud 容器设置为 Spring Boot 容器的父容器

			- BootstrapApplicationListener.DelegatingEnvironmentDecryptApplicationInitializer

				- 提前解密 Enviroment 环境里面的属性

			- PropertySourceBootstrapConfiguration

				- PropertySourceLocator 属性源定位器

			- EnvironmentDecryptApplicationInitializer

				- 解密 Enviroment 环境里面的属性

			- DelegatingApplicationContextInitializer

				- 外部化配置 context.initializer.classes = xxx,xxx

			- SharedMetadataReaderFactoryContextInitializer
			- ContextIdApplicationContextInitializer

				- 设置应用 ID -> applicationContext.setId()。默认取值为 spring.application.name，再为application，再为自动生成

			- ConfigurationWarningsApplicationContextInitializer

				- 错误的配置进行警告（不会终止程序），以 warn() 日志输出在控制台。默认内置的只有对包名的检查

			- RSocketPortInfoApplicationContextInitializer
			- ServerPortInfoApplicationContextInitializer

				- 将自己作为一个监听器注册到上下文 ConfigurableApplicationContext 里，专门用于监听 WebServerInitializedEvent 事件

					- ServletWebServerInitializedEvent
					- ReactiveWebServerInitializedEvent

			- ConditionEvaluationReportLoggingListener

				- 将 ConditionEvaluationReport 报告（自动配置中哪些匹配了，哪些没匹配上）写入日志，只有 LogLevel#DEBUG 时才会输出

	- 发送 ApplicationContextInitializedEvent 事件

		- BackgroundPreinitializer
		- DelegatingApplicationListener

### ApplicationPreparedEvent

- 上下文已准备好，应用上下文 ApplicationContext 初始化完成，该赋值的赋值了，Bean 定义信息也已全部加载完成。但是，单例 Bean 还没有被实例化，web 容器依旧还没启动。

	- 把 applicationArguments、printedBanner 等都作为一个 Bean 放进 Bean 工厂里
	- 若 lazyInitialization = true 延迟初始化，那就向 Bean 工厂放一个：new LazyInitializationBeanFactoryPostProcessor()
	- 根据 primarySources 和 allSources，交给 BeanDefinitionLoader（Sprig Boot 提供的实现）实现加载 Bean 的定义信息

		- AnnotatedBeanDefinitionReader

			- 基于注解

		- XmlBeanDefinitionReader

			- 基于 xml 配置

		- GroovyBeanDefinitionReader

			- Groovy 文件

		- ClassPathBeanDefinitionScanner

			- classpath 中加载

	- 发送 ApplicationPreparedEvent 事件

		- CloudFoundryVcapEnvironmentPostProcessor
		- ConfigFileApplicationListener

			- 向上下文注册一个 new PropertySourceOrderingPostProcessor(context)，Bean 工厂结束后对环境里的属性源进行重排序，把名字叫 defaultProperties 的属性源放在最末位

		- LoggingApplicationListener

			- 向工厂内放入 Log 相关 Bean

		- BackgroundPreinitializer

			- 本事件达到时无动作

		- RestartListener
		- DelegatingApplicationListener

			- 本事件达到时无动作

### ApplicationStartedEvent

- 应用成功启动，SpringApplication 的生命周期到这一步，正常的启动流程全部完成。Spring Boot 应用可以正常对对外提供服务。

	- 启动Spring容器

		- AbstractApplicationContext#refresh()

			- 实例化单例 Bean
			- 在 Spring 容器 refresh() 启动完成后， WebServer 也随之完成启动，成功监听到对应端口

	- 输出启动成功的日志
	- 发送ApplicationStartedEvent事件

		- BackgroundPreinitializer

			- 本事件达到时无动作

		- DelegatingApplicationListener

			- 本事件达到时无动作

		- TomcatMetricsBinder

			- tomcat 指标信息 TomcatMetrics绑 定到MeterRegistry，从而收集相关指标

	- callRunners()

		- 依次执行容器内配置的 ApplicationRunner/CommandLineRunner 的 Bean 实现类，支持 sort 排序

### ApplicationReadyEvent

- 应用已准备好，应用已经完完全全的准备好，并且也已经完成了相关组件的周知工作。

	- SpringApplicationAdminMXBeanRegistrar

		- 当此事件到达时，告诉 Admin Spring 应用已经ready，可以使用

	- BackgroundPreinitializer
	- DelegatingApplicationListener
	- RefreshEventListener

		- 当此事件到达时，告诉 Spring 应用已经 ready，接下来可以执行 ContextRefresher.refresh()

	- 还有一个过程就是 command-line runners 被调用的内容

### ApplicationFailedEvent

- 应用启动失败

	- LoggingApplicationListener

		- 执行 loggingSystem.cleanUp() 清理资源

	- ClasspathLoggingApplicationListener

		- 输出一句 debug 日志

	- BackgroundPreinitializer
	- DelegatingApplicationListener
	- ConditionEvaluationReportLoggingListener

		- 自动配置输出报告，输出错误日志

	- BootstrapApplicationListener.CloseContextOnFailureApplicationListener

		- 执行 context.close()

