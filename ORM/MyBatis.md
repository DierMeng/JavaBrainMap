# MyBatis

## MyBatis 快速入门

### ORM（Object Relational Mapping，对象-关系映射） 简介

- JDBC 查询操作步骤

	- 注册数据库驱动类，明确指定数据库 URL 地址、数据库用户名、密码等连接信息。
	- 通过 DriverManager 打开数据库连接
	- 通过数据库连接创建 Statement 对象。
	- 通过 Statement 对象执行 SQL 语句，得到 ResultSet 对象。
	- 通过 ResultSet 读取数据，并将数据转换成 JavaBean 对象。
	- 关闭 ResultSet、Statement 对象以及数据库连接，释放相关资源。

### 常见持久化框架

- Hibernate
- JPA（Java Persistence API）
- Spring JDBC
- MyBatis

### MyBatis 整体架构

- 基础支持层

	- 数据源模块

		- 连接池
		- 监控等

	- 事务管理模块

		- 多半与 Spring 集成，由 Spring 进行管理

	- 缓存模块

		- 一级缓存
		- 二级缓存

	- Binding 模块

		- 将用户自定义的 Mapper 接口与映射配置文件关联起来，系统可以通过调用自定义 Mapper 接口中的方法执行相应的 SQL 语句完成数据库操作，从而避免映射文件中定义的 SQL 节点拼写错误等问题。
		- 无须编写自定义 Mapper 接口的实现，MyBatis 会自动为其创建动态代理对象。

	- 反射模块

		- 对 Java 原生的反射进行了良好的封装，提供了更加简洁易用的 API，方便上层使调用，并且对反射操作进行了一系列优化，例如缓存了类的元数据，提高了反射操作的性能。

	- 类型转换

		- 别名
		- 实现 JDBC 类型与 Java 类型之间的转换

	- 日志模块

		- 提供详细的日志输出信息，还能够集成多种日志框架。

	- 资源加载

		- 对类加载器进行封装，确定类加载器的使用顺序，并提供了加载类文件以及其他资源文件的功能。

	- 解析器模块

		- 对 XPath 进行封装，为 MyBatis 初始化时解析 mybatis-config.xml 配置文件以及映射配置文件提供支持
		- 处理动态 SQL 语句中的占位符提供支持

- 核心处理层

	- 配置解析

		- 在 MyBatis 初始化过程中，会加载 mybatis-config.xml 配置文件、映射配置文件以及 Mapper 接口中的注解信息，解析后的配置信息会形成相应的对象并保存到 Configuration 对象中。 待 MyBatis 初始化之后，开发人员可以通过初始化得到 SqlSessionFactory 创建的 SqlSession 对象并完成数据库操作。

	- 参数映射

		- scripting 模块

			- 根据用户传入的实参，解析映射文件中定义的动态 SQL 节点，并形成数据库可执行的 SQL 语句。然后处理 SQL 语句中的占位符，绑定用户传入的实参。

	- SQL 解析

		- 动态 SQL 语句，提供多种动态 SQL 语句对应的节点，<where>、<if>、<foreach>等

	- SQL 执行

		- Executor

			- 主要负责维护一级缓存和二级缓存，并提供事务管理的相关操作，会将数据库相关操作委托给 StatementHandler 完成

		- StatementHandler

			- 首先通过 ParameterHandler 完成 SQL 语句的实参绑定，然后通过 java.sql.Statement 对象执行 SQL 语句并得到结果集

		- ParameterHandler
		- ResultHandler

			- 完成结果集的映射，得到结果对象并返回

	- 结果集映射
	- 插件

- 接口层

	- SqlSession

## 基础支持层

### 解析器模块

### 反射工具箱

### 类型转换

### 日志模块

### 资源加载

### DataSource

### Transaction

### binding 模块

### 缓存模块

- 装饰器模式
- Cache 接口及其实现

	- 方法声明

		- getId

			- 缓存对象的 ID

		- putObject

			- 向缓存中添加数据，一般情况下，key 是 CacheKey，value 是查询结果

		- getObject

			- 根据指定的 key，在缓存中查找对应的结果对象

		- removeObject

			- 删除 key 对应的缓存项

		- clear

			- 清空缓存

		- getSize

			- 缓存项的个数，该方法不会被 MyBatis 核心代码使用，所以可提供空实现

		- getReadWriteLock

			- 获取读写锁，该方法不会被 MyBatis 核心代码使用，所以可提供空实现

	- PerpetualCache

		- Cache 接口的基本实现
		- 底层使用 HashMap 记录缓存项，也是通过该 hashMap 对象的方法实现的 Cache 接口中定义的相应方法

	- 装饰类

		- BlockingCache

			- 阻塞版本的缓存装饰器，它会保证只有一个线程到数据库中查找指定 key 对应的数据

		- FifoCache

			- 在很多场景中，为了控制缓存的大小，系统需要按照一定的规则清理缓存。
			- FifoCache 是先入先出的装饰器，当向缓存添加数据时，如果缓存项的个数己经达到上限，则会将缓存中最老（即最早进入缓存）的缓存项删除。

		- LruCache

			- 按照近期最少使用算法进行缓存清理的装饰器，在需要清理缓存时，它会清除最近最少使用的缓存项。
			- 底层使用 LinkedHashMap

		- SoftCache & WeakCache

			- 在 SoftCache 中，最近使用的一部分缓存项不会被 GC 回收，这就是通过将其 value 添加到 hardLinksToAvoidGarbageCollection 集合中实现的（即有强引用指向其 value）
			- hardLinksToAvoidGarbageCollection 集合是 LinkedList<Object> 类型
			- WeakCache 的实现与 SoftCache 基本类似，唯一的区别在于其中使用 WeakEntry 封装真正的 value 对象

		- ScheduledCache

			- 周期性清理缓存的装饰器
			- clearInterval 属性记录两次缓存清理之间的时间间隔，默认是一小时
			- lastClear 属性记录最近一次清理的时间戳

		- LoggingCache

			- 在 Cache 的基础上提供了日志功能
			- requests 属性记录了缓存的缓存的访问次数
			- hits 属性记录了缓存的命中次数

		- SynchronizedCache

			- 通过在每个方法上添加 synchronized 关键字，为 Cache 添加了同步功能

		- SerializedCache

			- 提供将 value 对象序列化的功能
			- 在添加缓存项时，会将 value 对应的 Java 对象进行序列化，并将序列化后的 byte 数组作为 value 存入缓存
			- 在获取缓存项时，会将缓存项中的 byte 数组反序列化成 Java 对象。
			- 使用其他装饰器实现进行装饰之后，每次从缓存中获取同一 key 对应的对象时，得到的都是同一对象，任意一个线程修改该对象都会影响到其他线程以及缓存中的对象；

而 SerializedCache 每次从缓存中获取数据时，都会通过反序列化得到一个全新的对象。

- CacheKey

	- 表示缓存项的 key

		- 在 Cache 中唯一确定一个缓存项需要使用缓存项的 key, Mybatis 中因为涉及动态 SOL 等多方面因素，其缓存项的 key 不能仅仅通过一个 String 表示，所以 Mybatis 提供了 CacheKey 类来表示缓存项的 key，在一个 CacheKey 对象中可以封装多个影响缓存项的因素。
		- CacheKey 中可以添加多个对象，由这些对象共同确定两个 CacheKey 对象是否相同

	- 重要属性

		- multiplier

			- 参与计算 hashcode，默认值是 37

		- hashcode

			- CacheKey 对象的 hashcode，起始值是 17

		- checksum

			- 校验和

		- count

			- updateList 集合的个数

		- updateList

			- 由该集合中的所有对象共同决定两个 CacheKey 是否相同

## 高级主题

## 核心处理层

