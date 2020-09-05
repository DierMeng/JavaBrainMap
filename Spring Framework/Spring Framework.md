# Spring Framework

## Spring

### Spring 事务

- 优点

	- 统一编程模型，JDBC、Hibernate、JPA
	- 支持声明式事务（注解常用（Transactional）、xml）

		- @Transactional 失效的场景

			- 方法不是 public 的

				- Spring AOP 代理时，事务拦截器会检查目标方法的修饰符是否为 public

			- 异常捕获

				- catch 捕获异常之后进行自己的业务处理，所以不进行回滚

			- 方法 A 调用方法 B，但是 A 没有声明，B 声明了
			- 事务的传播属性设置为「support」相关
			- 数据库不支持
			- rollfallback 设置错误

				- Spring 事务默认 runtimeException 异常或者 Error 才回滚
				- 如果想自己指定异常类型进行回滚，需要使用 rollbackFor 指定异常类型

	- 编程式事务（TransactionTemplate）
	- 对spring数据层的完美抽象

- 传播性（TransactionDefinition）

	- PROPAGATION_REQUIRED

		- 如果当前没有事务，就新建一个事务，如果已经存在一个事务中，加入到这个事务中

	- PROPAGATION_REQUIRES_NEW

		- 创建一个新的事务，如果当前存在事务，则把当前事务挂起。

	- PROPAGATION_NESTED

		- 如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于PROPAGATION_REQUIRED。

	- PROPAGATION_SUPPORTS

		- 如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。

	- PROPAGATION_NOT_SUPPORTED

		- 以非事务方式运行，如果当前存在事务，则把当前事务挂起。

	- PROPAGATION_NEVER

		- 以非事务方式运行，如果当前存在事务，则抛出异常。

	- PROPAGATION_MANDATORY

		- 如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。

- 隔离级别

	- 子主题 1
	- 子主题 2
	- 子主题 3
	- 子主题 4

### AOP

按照一定的规则，将代码织入实现约定的流程当中。目的当然就是解耦、去重、服务增强。

- 动态代理

	- 当 Bean 有实现接口时，Spring 就会用 JDK 动态代理（JdkDynamicAopProxy）
	- 当 Bean 没有实现接口时，Spring 会选择 CGLIB 代理（CglibAopProxy）
	- Spring 可以通过配置强制使用 CGLIB 代理。

- 核心概念

	- 连接点（join point）

		- 在 Spring AOP 中通俗点指的就是具体被拦截，你要切入的方法。因为在 Spring AOP 中只支持方法的切入，是一种基于方法的 AOP，通过动态代理技术把它织入对应的流程中。

	- 通知（advice）

	  所谓「通知」就是按照规定将业务代码织入到连接点的前后了。

		- 前置通知（before advice）

			- 在目标方法被调用之前调用通知功能

		- 后置通知（after advice）

			- 在目标方法完成之后调用通知，是不管方法是否遇到异常都会执行的。

		- 环绕通知（around advice）

			- 环绕通知就是拿到目标方法，然后在目标方法可以自定义一些行为，然后在 Spring AOP 中通过叫 proceed() 的方法对目标方法进行主调实现环绕形式的通知。

		- 事后返回通知（after-returning advice）

			- 在目标方法执行成功之后调用通知，这里就是要求方法必须正常执行成功才会执行，遇到异常通知失效。

		- 异常通知（after-throwing advice）

			- 顾名思义，抛出异常后调用通知。

	- 切点（point cut）

	  正则表达式描述的某个方法，将这个方法抽象描述一下，避免代码重复。例如要在某个类的 HelloGlorze() 进行切入前置、后置等通知，在每个通知上面写同样的方法全限定路径是糟糕的行为。当然，我们的切面很多时候不单单应用于单个方法，它可能是多个类的不同方法，所以切点就是提供一种通过正则表达式以及指示器来定义和适配连接点的功能。

		- arg()/@args()

			- 限制连接点匹配参数为（指定类型、指定注解标注）的执行方法。

		- execution()

			- 用于匹配连接点的执行方法

		- this()

			- 限制连接点匹配 AOP 代理 Bean 引用为指定的类型

		- target/@target()

			- 目标对象/限制目标对象配置指定的注解

		- within/@within

			- 限制连接点匹配 指定/注解 类型

		- @annotation

			- 限定带有指定注解的连接点

	- 目标对象（target）

		- 就是被代理的对象，就是目标方法所属类的实例。

	- 引入（introduction）

		- 说白一点就是引入新的类和方法，来增强对目标方法的补充。

	- 织入（weaving）

	  这个其实就是所谓的「动态代理」技术，为原有的对象生成代理对象，然后根据切点拦截连接点，然后按照约定将引入的方法织入到流程当中。

		- 编译器时期切入

			- 切面会在目标类编译时期被织入，独立的 AspectJ 切面框架就支持这种织入方式。

		- 类加载器时期切入

			- 此时期切面会在目标类加载到 JVM（Java Virtual Machine，Java虚拟机）时被织入，当然这种方式是需要特定的类加载器才可以的，同样独立的 AspectJ 框架也支持这种方式织入切面

	- 切面（Aspect）

		- 上述切点、各类通知以及引入的定义集，他们集体定义了面向切面编程。在软件开发中，散布于应用中多处的功能被称为横切关注点（cross-cutting concern），以何种方式在何处应用，而无需修改受影响的类。横切关注点可以被模块化为特殊的类，这些类被称为切面（aspect）。所以通知和切点是切面的最基本元秦。

### 控制反转（IoC）、依赖注入（DI）

- 依赖注入的方式

	- 构造器注入

		- 采用反射的方式，通过使用构造方法来完成注入

	- Setter 方法注入

		- 消除了使用构造器注入时出现多个参数的可能性，首先可以把构造方法声明为无参数的，然后使用 setter 注入为其设置对应的值，也是通过 Java 反射技术得以现实。

	- 接口注入

		- 资源并非来自于自身系统，而是来自于外界，通过 JNDI 等形式去获取它，采用接口注入。

### Spring 管理 Bean 相关知识总结

- 装配 bean 的三种方式

	- 在 XML 中显式配置，由于现在基本是 Spring Boot 的天下了，所以现在 XML 的配置主键被淡化。

	  <bean id="User" class="com.glorze.entity.User">
	  	<property name="name"></property>
	  	<property name="age"></property>
	  </bean>

	- 在 Java 类中显式配置

		- @Bean 注解

	- 隐式的 bean 发现机制和自动装配

		- @Component 注解

		  声明这是一个 bean 组件，在 Spring 启动时就可以将这个类作为一个 bean 加入上下文中。

		- @Autowired 注解

		  如果两个类之间存在依赖时，就需要用到 @Autowired 注解，这个注解的作用就是将类自动注入到所用到的参数中。

- 自动装配 bean

	- byName 模式
	- byType 模式
	- 构造函数模式

		- 与 byType 模式功能上相同，只不过是使用的是构造函数而不是 setter 来执行注入

	- 默认模式

		- 自动在构造函数和 byType 模式之间选择

			- 如果 Bean 默认无参构造函数，就使用 byType 模式
			- 如果存在显示构造函数，就使用构造函数模式

	- 无

		- 这就是 Spring 的默认设置

- bean 的高级装配

	- bean 的作用域

		- 默认 bean 都是单例创建，不管一个 bean 被注入多少次都是同一个。
		- 创建 bean 的四种作用域

			- 单例（Singleton）在整个应用中，只创建 bean 的一个实例。
			- 原型（Prototype）每次注入或者通过 Spring 应用上下文获取的时候，都会创建一个新的 bean 实例。

				- @Scope(Configurablebeanfactory.SCOPE PROTOTYPE)
				- @Scope(“prototype”)

			- 会话（Session）在 Web 应用中，为每个会话创建一个 bean 实例。

				- @Scope(Configurablebeanfactory.SCOPE SESSION)

			- 请求（Rquest）在 Web 应用中，为每个请求创建一个 bean 实例。
			- @Scope

				- value：设置作用域
				- proxymode（针对会话和请求作用域），解决将会话或请求作用域的 bean 注入到单例 bean中所遇到的问题。

				  Spring 并不会将世纪的 oneBean 注入到 twoBean 中，Spring 会注入一个到 twoBean 的代理。这个代理会暴露与 oneBean 相同的方法，所以 twoBean 会认为 oneBean 就是一个「抽象」的bean。但是，当 twoBean 调用 oneBean 的方法是，代理会对其进行懒解析并将调用委托给作用域内真正的 oneBean。

					- Scopedproxymode.INTERFACES

						- 表明这个代理要实现某个 bean 的接口，并将调用委托给实现 bean。

					- Scopedproxymode.TARGET CLASS

						- bean 类型是具体的类，表明要以生成目标类扩展的方式创建代理。

		- 会话和请求作用域

- Spring Bean 结合源码理解生命周期（图解见当前目录下的「Spring Bean 生命周期图解」）

	- 获取 Bean 

		- 先处理 Bean 的名称，因为如果以「&」开头的 Bean 名称表示获取的是对应的 FactoryBean 对象；
		- 从缓存中获取单例 Bean，有则进一步判断这个 Bean 是不是在创建中，如果是的就等待创建完毕，否则直接返回这个 Bean 对象
		- 如果不存在单例 Bean 缓存，则先进行循环依赖的解析
		- 解析完毕之后先获取父类 BeanFactory，获取到了则调用父类的 getBean 方法，不存在则先合并然后创建 Bean

	- 创建 Bean

		- 创建 Bean 之前

			- 先获取 RootBeanDefinition 对象中的 Class 对象并确保已经关联了要创建的 Bean 的 Class 。
			- 检查 3 个条件

				- Bean 的属性中的  beforeInstantiationResolved 字段是否为 true，默认是 false。
				- Bean 是否是原生的 Bean
				- Bean 的  hasInstantiationAwareBeanPostProcessors 属性为 true，这个属性在 Spring 准备刷新容器前转为 BeanPostProcessors 的时候会设置，如果当前 Bean 实现了 InstantiationAwareBeanPostProcessor 则这个就会是 true。
				- 当三个条件都存在的时候，就会调用实现的 InstantiationAwareBeanPostProcessor 接口的 postProcessBeforeInstantiation 方法，然后获取返回的 Bean，如果返回的 Bean 不是 null 还会调用实现的 BeanPostProcessor 接口的 postProcessAfterInitialization 方法

		- 真正的创建 Bean（doCreateBean）

			- 先检查 instanceWrapper 变量是不是 null，这里一般是 null，除非当前正在创建的 Bean 在  factoryBeanInstanceCache 中存在的是保存还没创建完成的 FactoryBean 的集合。
			- 调用 createBeanInstance 方法实例化 Bean
			- 如果当前 RootBeanDefinition 对象还没有调用过实现了的   MergedBeanDefinitionPostProcessor 接口的方法，则会进行调用 。
			- 当满足三点

				- 是单例 Bean
				- 尝试解析 Bean 之间的循环引用
				- Bean 目前正在创建中
				- 会进一步检查是否实现了  SmartInstantiationAwareBeanPostProcessor 接口，如果实现了则调用是实现的  getEarlyBeanReference 方法

			- 调用 populateBean 方法进行属性填充
			- 调用 initializeBean 方法对 Bean 进行初始化

	- destory 方法销毁 Bean

### 缓存数据

- 循环依赖

	- spring单例对象的初始化

		- createBeanInstance：实例化

			- 调用对象的构造方法实例化对象

		- populateBean：填充属性

			- 对多个 bean 的依赖属性进行填充

		- initializeBean：调用 Spring XML 中的 init 方法

	- 构造器循环依赖

		- Spring 处理不了，会直接抛出BeanCurrentlylnCreationException 异常
		- 如在创建 TestA 类时，构造器需要 TestB 类，那将去创建 TestB，在创建 TestB 类时又发现需要 TestC 类，则又去创建 TestC，最终在创建 TestC 时发现又需要 TestA，从而形成一个环，没办法创建。

	- setter 循环依赖，单例循环依赖

		- 三级缓存

		  A 创建过程中需要 B，于是 A 将自己放到三级缓里面 ，去实例化 B；
		  
		  B 实例化的时候发现需要 A，于是 B 先查一级缓存，没有，再查二级缓存，还是没有，再查三级缓存，找到了A；
		  
		  接着 B 把三级缓存里面的 A 放到二级缓存里面，并删除三级缓存里面的 A；
		  
		  B 顺利初始化完毕，将自己放到一级缓存里面；
		  
		  然后回来接着创建 A，此时 B 已经创建结束，直接从一级缓存里面拿到 B ，然后完成创建，并将自己放到一级缓存里面。

			- 一级缓存：singletonObjects，singletonFactories，完成初始化的单例对象的缓存
			- 二级缓存：earlySingletonObjects，完成实例化但是尚未初始化的，提前曝光的单例对象的缓存
			- 三级缓存：singletonFactories，进入实例化阶段的单例对象工厂的缓存

	- 非单例循环依赖

		- 对于「prototype」作用域的 bean，Spring 容器无法完成依赖注入，因为 Spring 容器不进行缓存「prototype」作用域的 bean，因此无法提前暴露一个创建中的 bean 。

## Spring Boot

## Spring Cloud

## Spring MVC

### 核心流程

- 请求过来

	- DispatcherServlet

		- HandlerMapping

			- Controller

				- ModelAndView

		- ViewResolver
		- View

### 九大组件

- HandlerMapping，处理器映射器组件

	- HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;可自定义
	- 根据 request 找到相应的处理器 Handler 和 Interceptors

- HandlerAdapter

	- boolean supports(Object handler);判断是否可以使用某个 Handler。
	- ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;具体干活。
	- long getLastModified(HttpServletRequest request, Object handler);获取资源最后一次修改的时间。
	- 使用处理器干活的人。从名字上看，它就是一个适配器。因为 SpringMVC 中的 Handler 可以是任意的形式，只要能处理请求就可以，但是Servlet 需要的处理方法的结构却是固定的，都是以 request 和 response 为参数的方法。如何让固定的 Servlet 处理方法调用灵活的 Handler来进行处理就是 HandlerAdapter 要做的事情。

- HandlerExceptionResolver

	- ModelAndView resolveException(
			HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);
	- 根据异常设置 ModelAndView，再交给 render 方法进行渲染，分工明确互不干涉。

- ViewResolver

	- View resolveViewName(String viewName, Locale locale) throws Exception;
	- 将 String 类型的视图名和 Locale 解析为 View 类型的视图。

- RequestToViewNameTranslator

	- String getViewName(HttpServletRequest request) throws Exception;
	- 根据 ViewName 查找 View，但是有的 Handler 处理完后并没有设置 View 也没有设置 viewName，这个时候需要从 request 获取 viewName，这个过程就由 RequestToViewNameTranslator 完成。

- LocaleResolver，国际化 i18n 的处理

	- Locale resolveLocale(HttpServletRequest request);
	- void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale);
	- 解析视图需要两个参数：视图名和 Locale。LocaleResolver 用于从 request 解析出 Locale。

- ThemeResolver

	- String resolveThemeName(HttpServletRequest request);
	- void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName);
	- 用来设置页面主题，theme.properties 放到 classpath 下面。

- MultipartResolver，处理上传请求

	- boolean isMultipart(HttpServletRequest request);是不是上传请求。
	- MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException;将 request 包装成 MultipartHttpServletRequest
	- void cleanupMultipart(MultipartHttpServletRequest request);处理完后清理上传过程产生的临时资源。

- FlashMapManager，在重定向中传递参数。

	- FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response);用于恢复参数，并将回复过的和超时的参数从保存介质中删除
	- void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response);将参数保存。

### DispatcherServlet

- load-on-startup

	- 标记容器是否在启动的时候就加载这个servlet（实例化并调用其 init() 方法）。
	- 值必须是一个整数，表示 servlet 应该被载入的顺序
	- 值为 0 或者大于 0 时，表示容器在应用启动时就加载并初始化这个 servlet；
	- 值小于 0 或者没有指定时，则表示容器在该 servlet 被选择时才会去加载。
	- 正数的值越小，该 servlet 的优先级越高，应用启动时就越先加载。
	- 值相同时，容器就会自己选择顺序来加载。所以 load-on-startup 代表的是优先级，而非启动延迟时间。

## 自由主题

