# J2SE

## 线程、多线程

### 线程的状态

- 新创建（New）- 被创建但还没有调用 Start 方法
- 可运行（Runnable）- 可以在 JVM 中运行的状态，根据系统时间片决定是否正在运行的自己的代码
- 被阻塞（Blocked）

  当一个线程试图获取一个内部的对象锁（不是 java.util.concurrent 库中的锁），而该锁此时正被其他线程持有，则该线程进入阻塞状态；

- 等待（Waiting）- Object.wait、Thread.join、concurrent 中的 Lock 或者 Condition

  当线程等待另一个线程通知调度器一个条件时，它自己进入等待状态。

- 计时等待（Timed waiting）
- 被终止（Terminated）-因为 run 方法正常退出而死亡，或者因为没有捕获的异常终止了run方法而死亡。 

### 进程与线程

- 进程是操作系统的资源调度实体，有自己的内存地址空间和运行环境；
- 线程一般被称为轻量级的进程，线程和进程一样，也有自己的运行环境，但是创建一个线程要需要的资源比创建一个进程要少。线程存在于进程之中
- 每个进程至少有一个线程。一个进程下的多个线程之间可以共享进程的资源，包括内存空间和打开的文件。
- 进程跟程序（programs）、应用（applications）具备相同的含义，进程间通讯依靠 IPC（Inter-Process Communication，进程间通信） 资源，例如管道（pipes）、套接字（sockets）等；
- 线程间通讯依靠 JVM 提供的 API，例如 wait 方法、notify 方法和 notifyAll 方法，线程间还可以通过共享的主内存来进行值的传递；

### Thread

- 实现 Runnable 接口
- 方法

	- join()

		- 把指定的线程加入到当前线程，可以将两个交替执行的线程合并为顺序执行的线程。主线程必须等待子线程执行完毕才结束。
		- 如果在一个线程 A 中调用另一个线程 B 的 join 方法，线程 A 将会等待线程 B 执行完毕后再执行。

	- interrupted()

		- 清除中断状态

	- isInterrupted()

		- 不清除中断状态

	- start()

		- 开启一个线程，由新创建状态变为可运行状态

	- run()

		- 声明线程业务的方法，最终会被 start() 方法调用

	- holdsLock()

		- 检测线程是否持有锁

	- yield()

		- 暂停当前正在执行的线程对象，让其它有相同优先级的线程执行。它是一个静态方法而且只保证当前线程放弃 CPU 占用而不能保证使其它线程一定能占用 CPU，执行 yield() 的线程有可能在进入到暂停状态后马上又被执行。
		- 不会使当前线程阻塞

	- sleep()

		- 暂停当前线程并让出 cpu 的执行时间，不会释放当前持有的对象的锁资源，到时间后会继续执行。
		- 经常拿来与 Object.wait() 作对比，关于 wait() 参考面向对象分支。

	- suspend() 和 resume() 

		- 两个方法配套使用，suspend() 使得线程进入阻塞状态，并且不会自动恢复，必须其对应的 resume() 被调用，才能使得线程重新进入可执行状态。
		- JDK 1.5 中已经废除了这两个方法，因为存在死锁倾向

- 实现线程阻塞的方法

	- sleep()
	- Object.wait()
	- join()
	- 包含已经过时的 suspend

### 线程模型

- 主线程（每个进程只有一个主线程）

	- main() 方法

- 子线程

	- 非主线程的都是子线程

		- 守护线程

			- 为主线程提供一种通用服务的线程，比如 GC 线程。

		- 非守护线程，也称用户线程

			- 异步处理一些业务或逻辑
			- 用户线程在 start 之前可以通过 setDaemo(true) 来转变为守护线程
			- 如果在 start 之后调用 setDaemo(true)，将会 throw IllegalThreadStateException。

		- JVM 会一直运行，直到

			- 调用了 exit() 方法，并且 exit() 有权限被正常执行。
			- JVM中仅仅只有「守护线程」

- 内核线程

	- 由操作系统来直接支持与管理。
	- 除内核线程之外，所有涉及到线程、并发等概念的都可以叫做「用户线程」

- 内核线程与用户线程模型

	- 一对一（Java 语言采用）

		- 又叫作内核级线程模型，即一个用户线程对应一个内核线程，内核负责每个线程的调度，可以调度到其他处理器上面。

			- 优势

				- 实现简单

			- 劣势

				- 对用户线程的大部分操作都会映射到内核线程上，引起用户态和内核态的频繁切换；
				- 内核为每个线程都映射调度实体，如果系统出现大量线程，会对系统性能有影响；

	- 多对一（P\ython 的 gevent）

		- 又叫作用户级线程模型，即多个用户线程对应到同一个内核线程上，线程的创建、调度、同步的所有细节全部由进程的用户空间线程库来处理。

			- 优势

				- 用户线程的很多操作对内核来说都是透明的，不需要用户态和内核态的频繁切换，使线程的创建、调度、同步等非常快；

			- 劣势

				- 由于多个用户线程对应到同一个内核线程，如果其中一个用户线程阻塞，那么该其他用户线程也无法执行；
				- 内核并不知道用户态有哪些线程，无法像内核线程一样实现较完整的调度、优先级等；

	- 多对多（Go 语言中的 goroutine 调度器）

		- 又叫作两级线程模型，用户线程与内核线程是多对多（m : n，通常 m >= n）的映射模型。

			- 优势

				- 兼具多对一模型的轻量；
				- 由于对应了多个内核线程，则一个用户线程阻塞时，其他用户线程仍然可以执行；
				- 由于对应了多个内核线程，则可以实现较完整的调度、优先级等；

			- 劣势

				- 实现复杂

## 集合

### Collection

- List（默认大小为10）

	- ArrayList

		- 线程不安全
		- 检索快
		- JDK 8 下的扩容策略：
一、1.5倍扩容
二、如果超过 MAX_ARRAY_SIZE 进行 huge 扩容
三、MAX_ARRAY_SIZE 是 Integer 最大值-8，size 最大值就是 Integer.MAX_VALUE。
		- 简单源码分析

			- 父级继承实现

				- 实现了 RandomAccess 接口，可以随机访问
				- 实现了 Cloneable 接口，可以克隆
				- 实现了 Serializable 接口，可以序列化、反序列化
				- 实现了 List 接口，是 List 的实现类之一
				- 实现了 Collection 接口，是 Java Collections Framework 成员之一
				- 实现了 Iterable 接口，可以使用 for-each 迭代

			- 类特点

				- ArrayList 是实现 List 接口的可自动扩容的数组。实现了所有的 List 操作，允许所有的元素，包括 null 值。
				- ArrayList 大致和 Vector 相同，除了 ArrayList 是非同步的。
				- size isEmpty get set iterator 和 listIterator 方法时间复杂度是 O(1)，常量时间。其他方法是 O(n)，线性时间。
				- 每一个 ArrayList 实例都有一个 capacity（容量）。capacity 是用于存储列表中元素的数组的大小。capacity至少和列表的大小一样大。
				- 如果多个线程同时访问 ArrayList 的实例，并且至少一个线程会修改，必须在外部保证 ArrayList 的同步。修改包括添加删除扩容等操作，仅仅设置值不包括。这种场景可以用其他的一些封装好的同步的 list。如果不存在这样的Object，ArrayList 应该用 Collections.synchronizedList 包装起来最好在创建的时候就包装起来，来保证同步访问。
				- iterator() 和 listIterator(int) 方法是 fail-fast 的，如果在迭代器创建之后，列表进行结构化修改，迭代器会抛出 ConcurrentModificationException。
				- 面对并发修改，迭代器快速失败、清理，而不是在未知的时间不确定的情况下冒险。请注意，快速失败行为不能被保证。通常来讲，不能同步进行的并发修改几乎不可能做任何保证。因此，写依赖这个异常的程序的代码是错误的，快速失败行为应该仅仅用于防止 bug。

			- ArrayList 基本特点总结

				- ArrayList 底层的数据结构是数组
				- ArrayList 可以自动扩容，不传初始容量或者初始容量是0，都会初始化一个空数组，但是如果添加元素，会自动进行扩容，所以，创建 ArrayList 的时候，给初始容量是必要的
				- Arrays.asList() 方法返回的是的 Arrays 内部的 ArrayList，用的时候需要注意
				- subList() 返回内部类，不能序列化，和 ArrayList 共用同一个数组
				- 迭代删除要用，迭代器的 remove 方法，或者可以用倒序的 for 循环
				- ArrayList 重写了序列化、反序列化方法，避免序列化、反序列化全部数组，浪费时间和空间
				- elementData 不使用 private 修饰，可以简化内部类的访问

	- Vector

		- 线程安全
		- addElement(Object obj)、capacity()、add(int index, Object element)、contains(Object elem)
		- 四种构造方法

	- LinkedList

		- 双向链表
		- 数据操作快，删除，增加
		- 需要更多的内存
		- 线程不安全

			- List<String> list = Collections.synchronizedList(new LinkedList<String>());
			- 将 LinkedList 替换成 ConcurrentLinkedQueue

- Set

	- HashSet
	- SortedSet

- Queue

	- Deque

		- LinkedList

	- BlockingQueue
	- 声明的方法

		- boolean add(E e);

			- 将指定的元素添加入队列，如果队列是有界的且没有空闲空间则抛出异常。

		- boolean offer(E e);

			- 将指定的元素添加入队列，如果队列是有界的且没有空闲空间则返回 false。
			- 在使用有界队列时推荐使用该方法来替代 add 方法。

		- E remove();

			- 删除并返回队首的元素。如果队列为空则会抛异常。

		- E poll();

			- 删除并返回队首的元素。如果队列为空返回 null。

		- E element();

			- 返回队首元素但是不删除。如果队列为空会抛出异常。

		- E peek();

			- 返回队首元素但是不删除。如果队列为空则返回 null。

	- 实现类

		- 并发队列

			- ConcurrentLinkedQueue

		- 阻塞队列

			- ArrayBlockingQueue

				- 一个由数组结构组成的有界阻塞队列。

			- LinkedBlockingQueue

				- 一个由链表结构组成的有界阻塞队列。

			- PriorityBlockingQueue

				- 一个支持优先级排序的无界阻塞队列。

			- DelayQueue

				- 一个使用优先级队列实现的无界阻塞队列。

			- SynchronousQueue

				- 一个不存储元素的阻塞队列。

			- LinkedTransferQueue

				- 一个由链表结构组成的无界阻塞队列。

		- 双端队列

			- Deque
			- ArrayDeque
			- LinkedList
			- ConcurrentLinkedDeque

		- 优先级队列

			- PriorityQueue
			- PriorityBlockingQueue

### Map

- AbstractMap

	- HashMap（16/0.75）

	  HashMap 的数据结构是由 Node<k,v> 作为元素组成的数组：
	  一、如果有多个值 hash 到同一个桶中，则组织成一个链表，当这个链表的节点个数超过某个值（TREEIFY_THRESHOLD 参数指定）时，则将这个链表重构为一个二叉树；
	  二、如果发现 map 中的元素个数超过了 threshold，则进行二倍空间扩容。
	  三、HashMap 接受 value 为 null。
	  四、线程不安全。

		- LinkedHashMap

	- WeakHashMap
	- TreeMap
	- IdentityHashMap

- SortedMap

	- NavigableMap

		- TreeMap

- Hashtable

  HashTable 的数据结构和 HashMap 基本相同，但是属于线程安全，只不过是每次操作会锁住整个表结构，一次只能有一个线程访问 HashTable 对象。

### Dictionary

- Hashtable

## 输入输出流

### I/O

- 字节流

	- 就是万能流，什么都能读

- 字符流

	- 只能读取普通的文本

- 缓冲流

	- BufferedInputStream
	- BufferedOutputStream
	- BufferedReader
	- BufferedWriter

### NIO

## Java 类

### 成员变量

- 成员变量是可以不用给初始值的，默认就有一个初始值。

### 局部变量

- 局部变量，必须显示给予一个初始值，否则编译无法通过。

### 基本数据类型

- String

	- String 中 hashcode 的实现

		- 以 31 为权，每一位为字符的ASCII值进行运算，用自然溢出来等效取模。
		- 31 是一个奇质数，所以 31*i=32*i-i=(i<<5)-i，这种位移与减法结合的计算相比一般的运算快很多。

	- String 能存储的最大字符串数量

		- 编译期 65534 个
		- 运行期 Integer.MAX_VALUE 大约 4G

### 如何写一个不可变的类

- 类添加 final 修饰符，保证类不被继承。
- 保证所有成员变量必须私有，并且加上 final 修饰
- 不提供改变成员变量的方法，包括 setter
- 通过构造器初始化所有成员，进行深拷贝(deep copy)

## java.lang.annotation（Java 注解）

### 基本知识

- 注解（Annotation），也叫元数据（Metadata），是Java5的新特性
- 注解与类、接口、枚举在同一个层次，并可以应用于包、类型、构造方法、方法、成员变量、参数、本地变量的声明中，用来对这些元素进行说明注释。

### 注解的语法与定义形式

- 以 @interface 关键字定义
- 注解包含成员，成员以无参数的方法的形式被声明。其方法名和返回值定义了该成员的名字和类型。
- 成员赋值是通过 @Annotation(name=value) 的形式。
- 注解需要标明注解的生命周期，注解的修饰目标等信息，这些信息是通过元注解实现。

### 注解的分类

- 第一种分法

	- 基本内置注解

		- @Override
		- @Deprecated
		- @SuppressWarnings

	- 元注解，负责注解其他注解的注解

		- @Target

			- 标明注解的修饰目标，共有

		- @Retention
		- @Documented

			- 标记注解，用于描述其它类型的注解应该被作为被标注的程序成员的公共 API，因此可以被例如 javadoc 此类的工具文档化。

		- @Inherited

			- 标记注解，允许子类继承父类的注解

	- 自定义注解

- 第二种分法

	- 通过元注解 @Retention 实现，注解的值是 enum 类型的 RetentionPolicy

		- 用来修饰注解，是注解的注解，称为元注解。
		- RetentionPolicy

			- RetentionPolicy.SOURCE

				- 注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；
				- 对应 Java 源文件（.java文件）

			- RetentionPolicy.CLASS

				- 注解被保留到 class 文件，但 jvm 加载 class 文件时候被遗弃，这是默认的生命周期；
				- 对应 .class 文件

			- RetentionPolicy.RUNTIME

				- 注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
				- 内存中的字节码

			- 生命周期长度 SOURCE < CLASS < RUNTIME

		- 一般如果需要在运行时去动态获取注解信息，那只能用 RUNTIME 注解，比如 @Deprecated 使用 RUNTIME 注解
		- 如果要在编译时进行一些预处理操作，比如生成一些辅助代码就用 CLASS注解；
		- 如果只是做一些检查性的操作，比如 @Override 和 @SuppressWarnings，使用 SOURCE 注解。

## JDBC

### PreparedStatement

### Statement

### 数据库连接

- DataSource

	- DataSource 是作为 DriverManager 的替代品而推出的，DataSource 对象是获取连接的首选方法。
	- 类似于一个 DriverManager，拥有对外提供连接的能力,是一个工厂对象
	- DataSource 中获取的连接来自于连接池中，而池中的连接根本也还是从 DriverManager 获取而来
	- 形式是 JNDI （Java Naming Directory Interface）,在应用程序与数据库连接之间插入了一个中间层，进而可以实现连接池以及事务管理
	- 通过 DataSource 对象访问的驱动程序本身不会向 DriverManager 注册
	- CommonDataSource

		- DataSource

			- 获取 connection 的接口

		- XADataSource 

			- 用来获取分布式事务连接的接口

		- ConnectionPoolDataSource 

			- 从 connection pool 中拿 connection 的接口

	- 额外功能

		- 缓存 PreparedStatement 以便更快的执行
		- 可以设置连接超时时间
		- 提供日志记录的功能
		- ResultSet 大小的最大阈值设置
		- 通过 JNDI 的支持，可以为 servlet 容器提供连接池的功能

- DriverManager

	- Connection conn = DriverManager.getConnection(url, user, password); 建立与数据库的连接
	- 建立与数据库的连接是一项较耗资源的工作，频繁的进行数据库连接建立操作会产生较大的系统开销。

### JDBC 定义的五种事务隔离级别

- TRANSACTION_NONE

	- JDBC驱动不支持事务

- TRANSACTION_READ_UNCOMMITTED

	- 允许脏读、不可重复读和幻读。

- TRANSACTION_READ_COMMITTED

	- 禁止脏读，但允许不可重复读和幻读。

- TRANSACTION_REPEATABLE_READ

	- 禁止脏读和不可重复读，但允许幻读。

- TRANSACTION_SERIALIZABLE

	- 禁止脏读、不可重复读和幻读。

## 面向对象

### 抽象

### 封装

### 多态

### 继承

### Object

- getClass()
- equals(Object obj)，重写的时候必须覆写 hashCode()

	- 自反性：obj.equals(obj) 结果一定是 true。
	- 对称性：如果 obj1.equals(obj2) 结果为 true，那么obj2.equals(obj1) 也肯定是true。
	- 传递性：如果 obj1.equals(obj2) 结果为 true，obj2.equals(obj3) 结果也为 true，那obj1.equals(obj3) 肯定也是 true。
	- 一致性：对任意的 obj1 和 obj2，如果对象中用于等价比较的信息没有改变,那么无论调用 obj1.equals(obj2) 多少多少次，返回的结果是要保持一致的。即要么一直是 true，要么一直是 false。
	- 非空性：以上几条都不包含obj 为 null 的情况，而且对于任何非 null 的 obj，obj.equals(null) 都应该返回 false。

- hashCode()
- clone()

	- 浅拷贝、浅克隆
	- 深拷贝、深克隆

		- 一、构造函数，调用构造函数时进行深拷贝
		- 二、重载 clone() 方法
		- 三、序列化

- toString()
- notify()

	- 使用 notify() 的时候，在众多等待同一个锁的任务中只有一个会被唤醒，因此如果希望使用notify()，就必须保证被唤醒的是恰当的任务。

- notifyAll()

	- notifyAll() 将唤醒“所有正在等待的任务”。这并不是意味着在程序中任何地方，任何处于 wait() 状态中的任务都将被任何对 notify() 的调用唤醒，而是 notifyAll() 因某个特定锁而被调用的时候，只有等待这个锁的任务才会被唤醒。

- wait(三种重载)

	- 暂停当前线程并让出 cpu 的执行时间，会放弃所有锁并需要 notify/notifyAll 后重新获取到对象锁资源后才能继续执行。
	- 只能在同步方法或者同步块中使用。

- finalize（JDK9被标记过时）

	- 在 GC 决定回收一个不被其他对象引用的对象时调用。
	- 任何对象的 finalize 方法只会被  JVM 调用一次。

### Bean 的转换

- 分层领域模型对象

	- PO（Persistent Object）： 持久对象，数据；就是 DAO 层操作的对象。
	- BO（Business object） ：业务对象，封装对象、复杂对象 ，里面可能包含多个类；你可以当做 service 层（业务层）需要使用的。
	- DTO（Data Transfer Object）： 传输对象，前端调用时传输；
	- VO（View Object）：表现对象，前端界面展示。专门用来表现数据内容的，你可以理解为 SpringMVC 中 model 传输的那个对象，你可能会问它与 DTO 是什么区别，举个例子：当对象中有个 status 字段，它不需要展示在前端中，但是数据传输的时候需要用它来对前端用户做验证，验证用户状态是否正常，这个时候用到的就是 DTO，但是他不负责展示给用户，这么说吧 controller 接受 DTO，发送 VO
	- DO（Domain Object）：领域对象，就是从现实世界中抽象出来的有形或无形的业务实体。其实它一般也是和数据库中的表对应，更严谨一些。

- 转换工具

	- 自己对每个属性进行 setXX 方法的转换
	- Apache 的 BeanUtils 类

		- 性能差

	- Spring 的 BeanUtils

		- 调用次数足够多的时候，会明显的感受到卡顿

	- Cglib BeanCopier
	- MapStruct

		- 推荐

## Java 8 新特性

### Arrays 类的增强

- binarySearch、copyOf、copyOfRange、equals、fill、toString
- Java 8 与时俱进的增加了并发支持

	- XxxStream（Stream、IntStream、LongStream、DoubleStream）- 将数组转化为流式 API 进行操作，Stream 流也是 Java 8 新增的集合操作特性，下面有记录。
	- Spliterator<T> spliterator、Spliterator.OfInt spliterator、Spliterator.OfLong spliterator、Spliterator.OfDouble spliterator - 将数组元素转换成对应的 Spliterator 对象
	- parallel 系列增强，这些方法增加了并行能力，可以利用 CPU 并行来提高性能。

		- setAll：使用指定生成器为数组元素赋值
		- parallelSetAll：在 setAll d的基础上增强并行能力
		- parallelPrefix：使用 op 参数指定的算法将计算结果作为元数组得到的新元素结果。op 计算公式包括 left 和 right，分别代表前一索引处的元素和当前索引处元素。
		- parallelSort：跟原生 sort 方法一样，但是增强并行能力。

### 包装类的增强

- 基本数据类与对应的包装类

	- byte-Byte
	- short-Short
	- int-Integer

		- Integer 底层是现在在-128~127 有一个数组缓存,再次范围内的基本数据类型都从缓存数组中取值

	- long-Long
	- char-Character
	- float-Float
	- double-Double
	- boolean-Boolean

- 自动拆箱/自动装箱

	- 自动装箱：基本类型变量直接赋给包装类变量，或者赋给 Object
	- 自动拆箱：允许包装类对象直接赋给一个对应的基本数据类型变量

- Java 8 新增支持无符号运算

	- Integer、Long

		- toUnsignedString，将整数转换为无符号整数对应的字符串。
		- parseUnsignedXxx，将字符串解析成无符号整数。
		- compareUnsigned，将这个数转换为无符号整数在比较大小。
		- divideUnsigned，将整数转换为无符号整数然后计算他们相除的商。
		- remainderUnsigned，将整数转换为无符号整数然后计算他们相除的余数。

	- Byte、Short

		- toUnsignedInt
		- toUnsignedLong

### 改进的接口

- 允许定义默认方法，提供实现，default 修饰
- 允许定义类方法，static 修饰

### 改进的匿名内部类

- 在 Java 8 之前，Java 要求被局部内部类、匿名内部类访问的局部变量必须使用 final 修饰，从 Java 8 开始这个限制被取消。
- 如果局部变量被匿名内部类访问，那么该局部变量相当于自动使用了 final 修饰。
- 对于被匿名内部类访问的局部变量，可以用 final 修饰，也可以不用 final 修饰，但必须还得按照有 final 修饰的方式来用，不能重新赋值，叫做 effectively final。
- 匿名内部类的一些特点

	- 匿名内部类必须继承一个父类或实现一个接口且只能一个父类或者接口。
	- 匿名内部类不能是抽象类
	- 匿名内部类不能定义构造器
	- 匿名内部类只有一个隐式的无参构造器，new 接口名后的括号里面不能传参
	- 如果通过继承父类来创建匿名内部类，匿名内部类可以拥有父类的构造器。
	- 当创建匿名内部类时，必须实现接口或抽象父类里面的所有抽象方法。

### 新增的 Lambda 表达式（代码块作为方法参数，主要作用就是代替匿名内部类的繁琐语法）

- Lambda 基础

	- 形参列表
	- 箭头（->）
	- 代码块

- Lambda 表达式与函数式接口

	- 函数式接口：只包含一个抽象方法的接口
	- @FunctionalInterface 注解
	- Lambda 的限制

		- 目标类型必须是明确的函数式接口
		- 只能为函数式接口创建对象

	- java.util.function

		- XxxFunction->apply()：指定数据进行转换处理，返回新值。
		- XxxConsumer->accept()：同样负责参数处理，但是不返回处理结果。
		- XxxPredicate->test()：对参数进行某种判断，返回布尔值，用于数据筛选。
		- XxxSuplier->getAsXxx()：根据某周逻辑算法返回某种结果。

- 方法引用与构造器引用，两个冒号

	- 引用类方法，Integer::valueOf
	- 引用特定对象的实例方法，"str"::indexOf
	- 引用某类对象的实例方法，String::substring
	- 引用构造器，String::new

- Lambda 表达式与匿名内部类的联系与区别

	- 相同

		- 都可以直接访问「effectively final」以及外部类的成员变量
		- 生成的对象一样,都可以直接调用从接口中继承的默认方法

	- 不同

		- Lambda 表达式只能为函数式接口创建实例，而匿名内部类可以实现所有的抽象方法。
		- 匿名内部类可以为抽象类甚至普通类创建实例。
		- Lambda 不允许调用接口中定义的默认方法

### 日期/时间类

- java.time

	- Clock，获取指定时区的当前日期、时间，可以取代 System.currentTimeMills()
	- Duration，代表持续时间
	- Instant，代表一个具体的时刻，可以精确到纳秒。
	- LocalDate，代表不带时区的日期。
	- LocalTime，代表不带时区的时间
	- LocalDateTime，不带时区的日期、时间
	- MonthDay，代表月日
	- Year，年
	- YearMonth，年月
	- ZonedDateTime，时区化的日期、时间
	- ZoneId，代表一个时区
	- DayOfWeek，枚举类，定义周日到周六的枚举值
	- Month，枚举类，定义一月到十二月的枚举值。

### 集合的新增与改进

- Collection&Iterator 接口

	- 增强的 Iterator 遍历集合元素，forEachRemaining(Consumer action)
	- 新增的Predicate 操作集合，新增 removeIf(Predicate filter) 方法，批量删除符合 filter 条件的所有元素。
	- 新增的 Stream 操作集合，Stream、IntStream、LongStream、DoubleStream

		- 步骤

			- builder() 类方法创建对应的 Builder。
			- 重复调用 add() 向流中添加元素
			- 调用 build() 方法获取对应的 Stream
			- 调用 Stream 的聚集方法

		- 中间方法

			- filter、mapToXxx
			- peek、distinct
			- sorted、limit

		- 末端方法

			- forEach、toArray
			- reduce、count
			- min、max
			- anyMatch、allMatch
			- findFirst、findAny

		- 有状态的方法，给流添加一些新的属性

			- sorted
			- distinct
			- limit

		- 短路方法，尽早的结束对流的操作

			- limit

- List 集合

	- replaceAll(UnaryOperator operator)，根据指定的计算规则重新设置所有元素
	- sort(Comparator c)，根据 Comparator 参数对 List 进行排序。

- Map 集合

	- remove、putIfAbsent
	- compute、computeIfAbsent、computeIfPresent
	- forEach、getOrDefault、merge
	- replace、replaceAll

### 泛型,改进了泛型方法的类型推断能力

### JDBC

- executeLargeUpdate()，MySQL驱动暂不支持
- executeLargeBatch()

### 注解

- 重复注解
- 类型注解

### 多线程

- 改进的线程池

	- newWorkStealingPool(int parallelism)，创建持有足够线程的线程池来支持给定的并行级别。
	- newWorkStealingPool，目标并行级别等于当前机器 CPU 数量。

- 增强的 ForkJoinPool，通用池

	- commonPool()，返回一个通用池
	- getCommonPoolParallelism()，返回通用池的并行级别

### 反射，新增方法参数反射

- getParameterCount()，获取构造器或方法的形参个数
- Parameter[] getParameters()

	- getModifiers()、getName()
	- getParameterizedType()、getType()
	- isNamePresent()、isVarArgs

## Java 9 新特性

### 最主要的变化是已经实现的模块化系统，是一个包的容器

### HTTP 2 客户端

### 改进的 Javadoc

### 多版本兼容 JAR 包

### 集合工厂方法

### 私有接口方法

### 进程 API

### 改进的 Stream API

### 改进 try-with-resources

### 改进的弃用注解 @Deprecated

### 改进钻石操作符(Diamond Operator) 

### 改进 Optional 类

### 改进的 CompletableFuture API

### 响应式流（Reactive Streams) API

## Java 10 新特性

### 局部变量类型推断

- 引入了 var 语法，可以自动推断变量类型

### GC改进和其他内务管理

### 线程本地握手

### 备用内存设备上的堆分配

### 根证书认证程序

## Java 11 新特性

### Java 8 之后的第一个 LTS 版本，从 Java 11 开始，Oracle JDK 不在以免费的用于商业用途

### String API

- isBlank() 判空
- lines() 分割获取字符串流
- repeat() 复制字符串
- strip() 去除前后空白字符

### File API

### HTTP Client

- 支持 HTTP/1.1 和 HTTP/2 ，也支持 websockets。

### Lambda 局部变量推断

### 单命令运行 Java

### 免费的飞行记录器

## Java 12 新特性

### Switch 表达式

### Shenandoah GC

### JVM 常量 API

### G1的可中断 mixed GC

### G1归还不使用的内存

## Java 13 新特性

### GC 层面则改进了 ZGC，以支持 Uncommit Unused Memory

### 语法层面，改进了 Switch Expressions，新增了 Text Blocks，二者皆处于 Preview 状态；API 层面主要使用 NioSocketImpl 来替换 JDK1.0 的 PlainSocketImpl

## Java 14 新特性

### instanceof 的模式匹配

- 对目标对象进行检查的断言（predicate）
- 当断言成立时，从目标对象中提取值的绑定变量。

### Java 打包工具

### G1 支持 NUMA

### 更有价值的 NullPointerException

### 删除 CMS 垃圾回收器

### 废弃 ParallelScavenge + SerialOld 的 GC 组合

## Java 15 新特性

### 外内存访问 API

### 密封类（sealed classes）的预览

### 数字签名算法

### 套接字的更新实现

### instanceof模式匹配

### ZGC 产品化

### RMI Activation 进入不推荐期

