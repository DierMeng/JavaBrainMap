# Java 日志体系

## 日志门面

门面指的是设计模式中的「外观模式」，关于更多资料可以参考一下老四的《浅析设计模式第十二章之外观模式》这篇文章。http://www.glorze.com/483.html

### JCL（Jakarta Commons Logging），就是现在我们所说的 Apache Commons Logging

- 实现机制：程序运行时，使用自己的 ClassLoader 寻找和载入本地具体的实现。

  ClassLoader 是 Java 类中的加载器，相关知识点可以参考一下老四写的《浅析Java反射系列相关基础知识(上)之类的加载以及反射的基本应用》这篇文章。http://www.glorze.com/1180.html

### SLF4J（Simple Logging Facade for Java）

- 实现机制：Slf4j 在编译期间，静态绑定本地的 Log 库
- 核心类与接口

	- org.slf4j.LoggerFactory(class)

		- 给调用方提供的创建Logger的工厂类，在编译时绑定具体的日志实现组件

	- org.slf4j.Logger(interface)

		- 给调用方提供的日志记录抽象方法，例如debug(String msg),info(String msg)等方法

	- org.slf4j.ILoggerFactory(interface)

		- 获取的Logger的工厂接口，具体的日志组件实现此接口

	- org.slf4j.helpers.NOPLogger(class)

		- 对org.slf4j.Logger接口的一个没有任何操作的实现，也是Slf4j的默认日志实现

	- org.slf4j.impl.StaticLoggerBinder(class)

		- 与具体的日志实现组件实现的桥接类，具体的日志实现组件需要定义org.slf4j.impl包，并在org.slf4j.impl包下提供此类

- 与其他各种日志组件的桥接

	- logback-classic-X.X.XX.jar

		- Slf4j 的原生实现，Logback 直接实现了 Slf4j 的接口，因此使用 Slf4j 与 Logback 的结合使用也意味更小的内存与计算开销

	- slf4j-log4jXX-X.X.XX.jar

		- 需要将Log4j.jar加入Classpath

	- slf4j-jdkXX-X.X.XX.jar

		- java.util.logging的桥接器，Jdk原生日志框架。

	- slf4j-jcl-X.X.XX.jar

		- Jakarta Commons Logging 的桥接器. 这个桥接器将 Slf4j 所有日志委派给 Jcl。

	- slf4j-nop-X.X.XX.jar

		- NOP 桥接器，默默丢弃一切日志。

	- slf4j-simple-X.X.XX.jar

		- 一个简单实现的桥接器，该实现输出所有事件到System.err. 只有Info以及高于该级别的消息被打印，在小型应用中它也许是有用的。

### JBoss Logging

## 日志实现

### Log4j

- apache实现的一个开源日志组件。

### Log4j2

- 为了对抗 LogBack，对 Log4j 进行了全部重写，不兼容 Log4j。

### LogBack

- 其实 LogBack 的作者就是 Log4j 的作者，因为他不喜欢 Apache 公司的工作方式，自己开了公司又写了 Slf4j。

### JUL（Java Util Log）

## 常用的组合使用方式

### Commons Logging 与 Log4j 组合使用

- 随着 Slf4j 的性能优良，使用越来越高，这个组合逐渐被淡化。相比之下开销比较高。

### Slf4j 与 Logback 组合使用

LogBack 必须与 Slf4j 使用，因为是同一个作者，保证兼容性，别瞎用。

- 优势

	- Slf4j 实现机制决定 Slf4j 限制较少，使用范围更广。由于 Slf4j 在编译期间，静态绑定本地的 LOG 库使得通用性要比 Commons Logging 要好。
	- Logback拥有更好的性能，更快的执行速度。Logback声称：某些关键操作，比如判定是否记录一条日志语句的操作，其性能得到了显著的提高。这个操作在Logback中需要3纳秒，而在Log4J中则需要30纳秒。LogBack创建记录器（logger）的速度也更快：13毫秒，而在Log4J中需要23毫秒。更重要的是，它获取已存在的记录器只需94纳秒，而Log4J需要2234纳秒，时间减少到了1/23。跟JUL相比的性能提高也是显著的。
	- 充分的测试
	- logback-classic 非常自然的实现了 SLF4J
	- 丰富的扩展文档
	- 可以使用使用 XML 配置文件或者 Groovy
	- 自动重新载入配置文件
	- 优雅地从 I/O 错误中恢复
	- 自动清除旧的日志归档文件
	- 自动压缩归档日志文件
	- Logback 所有文档免费，Log4j 还有付费支持项
	- 软件工程的角度。抽象，解耦，便于维护。
	- 语法设计角度。slf4j 有 {} 占位符，而 log4j 需要用「+」来连接字符串，既不利于阅读，同时消耗了内存。

