# Spring Framework 源码

## spring-aop

### AspectJAutoProxyBeanDefinitionParser，解析器，一旦遇到 aspectj-autoproxy 注解时就会使用这个解析器接口，在parse() 方法中进行注册；动态 AOP 自定义标签

- registerAspectJAnnotationAutoProxyCreatorIfNecessary

	- registerAspectJAnnotationAutoProxyCreatorIfNecessary，注册或升级 AutoProxyCreator 定义 beanName 为 internalAutoProxyCreator 的 BeanDefinition

		- registerOrEscalateApcAsRequired，自动注册 AnnotationAwareAspectJAutoProxyCreator 类的功能，该方法存在一个优先级判断：如果存在自动代理创建器，并且与现在的不一致，需要根据优先级来判断使用哪个

	- useClassProxyingIfNecessary，对于 proxy-target-class 以及 expose-proxy 属性的处理

		- JDK 动态代理

			- 如果被代理的目标对象实现了至少一个接口，则会使用JDK动态代理。所有该目标类型实现的接口都将被代理。
			- 代理对象必须是某个接口的实现，它是通过在运行期问创建一个接口的实现类来完成对目标时象的代理。

		- CGLIB 代理

			- 若该目标时象没有
实现任何接口，则创建一个CGLIB代理。
			- 实现原理类似于JDK动态代理，只是它在运行期间生成的代理对象是针时目标类扩展的子类。CGLIB 是高效的代码生成包，底层是依靠 ASM(开源的 Java 字节码编样类库)操作字节码实现的.性能比 JDK 强。
			- JDK 本身就提供了动态代理，强制使用 CGLI B代理需要将 <aop:config> 的 proxy-target-class 属性设为 true：
<aop:config proxy-target-class="true">...</aop:config>
			- 当需要使用 CGLIB 代理和 @Aspect 自动代理支持，可以按照以下方式设置<aop:aspectj-autoproxy> 的 proxy-target-class 属性为 true：
<aop:aspectj-autoproxy proxy-target-class="true"/>

		- expose-proxy

			- 解决有时候目标对象内部的自我调用将无法实施切面中的增强

	- registerComponentIfNecessary，注册组件并通知，便于监听器进一步处理

### AnnotationAwareAspectJAutoProxyCreator，创建 AOP 代理

- postProcessAfterInitialization，父类 AbstractAutoProxyCreator 的方法，在这里实现代理的创建 

	- getAdvicesAndAdvisorsForBean

		- 获取增强方法或者增强器
		- 根据获取的增强进行代理

- findCandidateAdvisors，获取增强器

	- buildAspectJAdvisors

		- 获取所有 beanName，这一步骤中所有在 beanFacotry 中注册的 bean 都会被提取出来。
		- 遍历所有 beanName，并找出声明 AspectJ 注解的类，进行进一步的处理。
		- 对标记为 AspectJ 注解的类进行增强器的提取。

			- 普通增强器的获取

				- 切点信息的获取
				- 根据切点信息生成增强

			- 增加同步实例化增强器

				- 如果寻找的增强器不为空而且又配置了增强延迟初始化，那么就需要在首位加入同步实例化增强器 - SyntheticInstantiationAdvisor。

			- 获取 DeclareParents 注解

		- 将提取结果加入缓存。

- findAdvisorsThatCanApply，寻找匹配的增强器

	- findAdvisorsThatCanApply

		- 寻找所有增强器中适用于当前 class 的增强器。引介增强与普通的增强处理是不一样的，所以分开处理。

	- canApply

		- 真正的匹配在 canApply 
中实现。

- createProxy

	- 获取当前类中的属性
	- 添加代理接口
	- 封装 Advisor 并加入到 ProxyFactory 中

		- 创建代理

			- 影响 Spring 使用 JDKProxy 还是 CglibProxy 的判断

				- optimize

					- 用来控制通过 CGLIB 创建的代理是否使用激进的优化策略。目前这个属性仅用于CGLIB代理，对于 JDK 动态代理(默认代理)无效。

				- ProxyTargetClass

					- 这个属性为 true 时，目标类本身被代理而不是目标类的接口。如果这个属性值被设为true, CGLIB 代理将被创建，设置方式为<aop:aspectj-autoproxy-proxy-target-class="true">。

				- hasNoUserSuppliedProxyInterfaces

					- 是否存在代理接口

			- JDK 与 Cglib 方式的总结

				- 如果目标时象实现了接口，默认情况下会采用 JDK 的动态代理实现 AOP。
				- 如果目标时象实现了接口，可以强制使用 CGLIB 实现 AOP。
				- 如果目标对象没有实现接口，必须采用 CGLIB 库，Spring 会自动在 JDK 动态代理和 CGLIB 之间转换。

			- 如何强制使用 CGLIB 实现 AOP？

				- 添加 CGLIB 库，Spring_HOME/cglib/*.jar
				- 在 Spring 配置文件中加入 <aop:aspectj-autoproxy proxy-target-class="true"/>。

			- JDK 动态代理和 CGLIB 字节码生成的区别？

				- JDK 动态代理只能时实现了接口的类生成代理，而不能针对类。
				- CGLIB 是针对类实现代理，主要是对指定的类生成一个子类，覆盖其中的方法，因为
是继承，所以该类或方法最好不要声明成 final。

		- 获取代理

			- JDK 代理

				- 业务接口
				- 业务接口实现类
				- 自定义 InvocationHandler

					- 构造函数，将代理的对象传入
					- invoke 方法，实现了 AOP 增强的所有逻辑
					- getProxy 千篇一律，但是必不可少

				- JdkDynamicAopProxy

					- getProxy(@Nullable ClassLoader classLoader)
					- invoke(Object proxy, Method method, Object[] args)

					  创建了一个拦截器链，并使用 ReflectiveMethodlnvocation 类进行了链的封装，而在 ReflectiveMethodlnvocation 类的 proceed 方法中实现了拦截器的逐一调用。

			- CGLIB

				- CglibAopProxy

					- getProxy(@Nullable ClassLoader classLoader)
					- getCallbacks(Class<?> rootClass)
					- DynamicAdvisedInterceptor，内部类

						- intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy)

	- 设置要代理的类
	- 进行获取代理操作

### 创建静态 AOP

AOP 的静态代理主要是在虚拟机启动时通过改变目标对象字节码的方式来完成对目标对象的增强，它与动态代理相比具有更高的效率，因为在动态代理调用的过程中，还需要一个动态创建代理类并代理目标对象的步骤，而静态代理则是在启动时便完成了字节码增强，当系统再次调用目标类时与调用正常的类并无差别，所以在效率上会相对高些。

- Instrumentation 使用
- 自定义标签

	- 是否开启 AspectJ
	- 将 org.Springframework.context.weaving.AspectJWeavingEnabler 封装在 BeanDefinition 中

- 织入

	- AspectJWeavingEnabler 类型的 bean 中的 loadTimeWeaver 属性被初始化为 DefaultContextLoadTimeWeaver 类型的 bean。
	- DefaultContextLoadTimeWeaver 类型的 bean 中的 loadTimeWeaver 属性被初始化为 InstrumentationLoadTimeWeaver。

## Spring MVC

### web.xml

- contextConfigLocation

	- Web 与 Spring 的配置文件相结合的配置参数，Spring、MVC 初始化的开端

- DispatcherServlet

	- 包含 SpringMVC 的请求逻辑，使用这个类拦截 Web 请求并进行相应的业务逻辑处理。

### ContextLoaderListener

- 实现 ServletContextListener 接口，从而实现 contextInitialized 方法，每一个 Web 应用都有一个 ServletContext 与之关联，启动创建，关闭销毁。
- contextInitialized(ServletContextEvent event)

	- initWebApplicationContext(ServletContext servletContext)，初始化的大致步骤

		- WebApplicationContext 存在性的校验

		  查看 ServletContext 实例中是否有对应 key 的属性。这个 key 就是「String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";」

		- 创建 WebApplicationContext 实例

			- createWebApplicationContext(ServletContext sc)

			  在初始化的过程中，程序首先会读取 Contextloader 类的同目录下的属性文件 ContextLoader.properties，并根据其中的配置提取将要实现 WebApplicationContext 接口的实现类，并根据这个实现类通过反射的方式进行实例的创建。

		- 将实例记录在 servletContext 中。
		- 映射当前的类加载器与创建的实例到全局变量 currentContextPerThread 中。

### DispatcherServlet

### DispatcherServlet 逻辑处理

## 事务

### 事务自定义标签

- TxNamespaceHandler.init

	- 解析 《tx:annotation-driven 开头的配置
	- 使用 AnnotationDrivenBeanDefinitionParser 类的 parse 方法进行解析

- 注册 InfrastructureAdvisorAutoProxyCreator

	- AnnotationDrivenBeanDefinitionParser.AopAutoProxyConfigurer.configureAutoProxyCreator

		- AnnotationTransactionAttributeSource
		- TransactionInterceptor

			- invoke 方法完成整个事务的逻辑

		- BeanFactoryTransactionAttributeSourceAdvisor，上面两个 bean 注册到了这个类的实例里面

	- AbstractAutoProxyCreator.wrapIfNecessary

		- 找出指定 bean 对应的增强器
		- 根据找出的增强器创建代理

- 获取对应 class/method 的增强器

	- 寻找候选增强器：AbstractAdvisorAutoProxyCreator.findCandidateAdvisors

		- 获取增强器

	- 候选增强器中寻找到匹配项：AopUtils.findAdvisorsThatCanApply

		- 增强器是否与对应的 class 匹配

	- 提取事务标签：AbstractFallbackTransactionAttributeSource.computeTransactionAttribute

		- 提取事务标签

### 事务增强器

- 声明式事务处理步骤（TransactionAspectSupport.invokeWithinTransaction）

	- 获取事务的属性
	- 加载配置中配置的 TransactionManager
	- 不同的事务处理方式使用不同的逻辑
	- 在目标方法执行前获取事务并收集事务信息
	- 执行目标方法
	- 一旦出现异常，常识异常处理，默认只对 RuntimeException 回滚
	- 提交事务前的事务信息清除
	- 提交事务

- 创建事务

	- TransactionAspectSupport.createTransactionIfNecessary

		- 使用 DelegatingTransactionAttribute 封装传人的 TransactionAttribute 实例。
		- 获取事务
		- 构建事务信息

	- 获取事务

		- 事务的准备工作：AbstractPlatformTransactionManager.getTransaction

			- 获取事务
			- 如果当前先线程存在事务则转向嵌套事务的处理
			- 事务超时设置验证
			- 事务 propagationBehavior 属性的设置验证
			- 构建 DefaultTransactionStatus
			- 完善 transaction，包括设置 ConnectionHolder、隔离级别、timeout，如果是新连接，则绑定到当前线程

		- 事务的开始：DataSourceTransactionManager.doBegin，构造 transaction，包括设置 ConnectionHolder、隔离级别、timeout；如果是新连接，绑定到当前线程

			- 尝试获取连接
			- 设置隔离级别以及只读标识
			- 更改默认的提交设置
			- 设置标志位，标识当前连接已经被事务激活
			- 设置过去时间
			- 将 connectionHolder 绑定到当前线程
			- 将事务信息记录在当前线程中

	- 处理已经存在的事务：AbstractPlatformTransactionManager.handleExistingTransaction
	- 准备事务信息：TransactionAspectSupport.prepareTransactionInfo

- 回滚处理：TransactionAspectSupport.completeTransactionAfterThrowing

	- 回滚条件：DefaultTransactionAttribute.rollbackOn

		- 默认情况下 Spring 中的事务异常处理机制只对 RuntimeException 和 Error两种情况感兴趣

	- 回滚处理：AbstractPlatformTransactionManager.rollback

		- 自定义触发器的调用，包括回滚前、完成回滚后的调用
		- 真正的回滚逻辑

			- 当之前已经保存的事务信息中有保存点信息的时候，使用保存点信息进行回滚。常用于嵌入式事务，对于嵌入式的事务的处理,内嵌的事务异常并不会引起外部事务的回滚。
			- 当之前已经保存的事务信息中的事务为新事务，那么直接回滚。常用于单独事务的处理对于没有保存点的回滚。
			- 当前事务信息中表明是存在事务的，又不属于以上两种情况，多数用于 JTA，只做回滚标识，等到提交的时候统一不提交。

	- 回滚后的信息清除：AbstractPlatformTransactionManager.cleanupAfterCompletion

		- 设置状态是对事务信息作完成标识以避免重复调用。
		- 如果当前事务是新的同步状态，需要将绑定到当前线程的事务信息清除。
		- 如果是新事务需要做些清除资源的工作。
		- 如果在事务执行前有事务挂起，那么当前事务执行结束后需要将挂起事务恢复。

- 事务提交：TransactionAspectSupport.commitTransactionAfterReturning

	- 符合提交的条件

		- 当事务状态中有保存点信息的话便不会去提交事务
		- 当事务非新事务的时候也不会去执行提交事务操作。

### Spring 事务核心 API 类总结

- TransactionDefinition

	- 给定的事务规则
	- 用于定义一个事务
	- 包含了事务的静态属性，比如：事务传播行为、超时时间等等
	- 默认实现类：DefaultTransactionDefinition

- PlatformTransactionManager

	- 按照规则来执行提交或者回滚操作，也就是用于执行具体的事务操作

		- TransactionStatus getTransaction(TransactionDefinition definition)
		- void commit(TransactionStatus status)
		- void rollback(TransactionStatus status)

	- 主要实现类

		- DataSourceTransactionManager 

			- 适用于使用 JDBC  和MyBatis 进行数据持久化操作的情况。

		- HibernateTransactionManager 

			- 适用于使用 Hibernate 进行数据持久化操作的情况。

		- JpaTransactionManager 

			- 适用于使用 JPA 进行数据持久化操作的情况。

		- JtaTransactionManager 
		- JdoTransactionManager
		- JmsTransactionManager

- TransactionStatus

	- 表示一个运行着的事务的状态

## IoC & DI

### BeanDefinition 运行时序（xml web 配置文件为例）

- BeanFactory

	- ListableBeanFactory

		- 可列表化

	- HierarchicalBeanFactory

		- 负责 bean 的继承关系

	- AutowireCapableBeanFactory

		- 负责 Bean 的自动注入

- BeanDefinition，实例化之前都用 BeanDefinition 描述 Bean，负责定义的各种 Bena 对象及其相互关系
- BeanDefinitionReader

	- 负责对 Spring 配置文件的解析

- 资源定位

	- ClasspathXmlApplicationContext
SystemFileXmlApplicationContext
XmlWebApplicationContext

		- 执行构造，加载资源文件

	- AbstractRefreshableConfigApplicationContext

		- refresh()

			- 启动 IoC 的入口，IoC  容器的初始化，如果已经存在会执行覆盖操作，规定了 IoC 的启动流程，对 Bean 配置资源进行载入

		- loadBeanDefinitions()

	- AbstractXmlApplicationContext

		- ConfigurableListableBeanFactory()

- 加载

	- AbstractXmlApplicationContext

		- loadBeanDefinitions()，通过 XmlBeanDefinitionReader 的 doLoadBeanDefinitions() 方法加载配置文件中声明的 Bean

	- XmlBeanDefinitionReader

		- doLoadBeanDefinitions()

- 注册（registerBeanDefinition）

	- DefaultBeanDefinitionDocumentReader
	- BeanDefinitionParserDelegate
	- BeanDefinitionReaderUtils

		- 解析 bean 元素的过程中不对 Bean 进行实例化，始终使用 BeanDefinition 来描述

	- DefaultListableBeanFactory

		- 真正的完成注册功能
		- 维护一个 HashMap，存放 BeanDefinition

### 基于注解的 IoC 初始化

- 相对于配置文件，区别在于扫描读取的方式不同
- 定位 Bean 扫描路径

	- AnnotationConfigApplicationContext
	- AnnotationConfigWebApplicationContext

- 读取注解的元数据
- 扫描包并解析为 BeanDefinition
- 注册

### Spring 管理 Bean 的生命周期

- 获取 Bean

	- 三级缓存获取 Bean，三级缓存解决的是循环依赖，针对单例 Bean

- 创建 Bean

	- 创建之前的预处理

		- 使用 BeanDefinition 对 Bean 进行描述，处理对象间的各种依赖关系，对 Bean 进行空校验等一系列操作，也包括容器刷新的一些后置处理器的处理等操作

	- 真正的创建 Bean

		- doCreateBean

			- createBeanInstance

				- 通过构造实例化 Bean

			- populateBean

				- 对 Bean 进行填充
				- 后置处理器

			- initializeBean

				- 初始化前后处理器的处理

- 销毁 Bean

	- destory

