# 深入理解 Java 虚拟机 JVM 高级特性与最佳实践

## 2.Java 内存区域与内存溢出异常

### 概述

###                                运行时数据区域

- 程序计数器

	- 当前线程所执行的字节码的行号指示器
	- 唯一一个在 JVM 中没有规定任何 OutOfMemoryError 情况的区域
	- 线程私有：一个处理器智慧之星一条线成中的指令，为了能恢复到正确的执行为之，每个线程都需要有一个独立的程序计数器

- Java 虚拟机栈

	- 线程私有，生命周期与线程相同
	- 每个方法执行的同时都会创建一个栈帧，局部变量表、操作数栈、动态连接、返回地址等。
	- 存储局部变量表(boolean、byte、char、short、int、float、long、double、对象引用、returnAddress)
	- 两种异常状况

		- StackOverflowError

			- 如果线程请求的栈深度大于虚拟机所允许的深度

		- OutOfMemoryError

			- 虚拟机栈动态扩展时无法申请到足够的内存

- 本地方法栈

	- 与虚拟机栈作用相似，虚拟机栈负责执行 Java 方法字节码服务，本地方法栈负责 Native 方法服务
	- 也会抛出 StackOverflowError 和 OutOfMemoryError 异常

- Java 堆

	- 被所有线程共享，作用就是给几乎所有的对象实例（包括数组）分配内存
	- 垃圾收集器管理的主要区域，所以也叫「GC 堆」
	- 分类（分代收集算法）

		- 新生代

			- Eden 空间
			- From Survivor 空间
			- To Survivor 空间

		- 老年代

	- 分类（内存分配）

		- 多个线程私有的分配缓冲区

	- 分类的目的都是更好的回收内存和分配内存
	- 如果在堆中没有内存完成实例分配，并且堆也无法再扩展时，会抛出 OutOfMemoryError 异常

- 方法区，JDK8 以后可以叫做「元空间」，撤销永久代的概念

	- 方法区（Method Area）存储

		- 运行时常量
		- 已被虚拟机加载的类信息
		- 静态变量
		- 即时编译器编译后的代码
		- 运行时常量池

	- 当方法区无法满足内存分配需求时，将抛出 OutOfMemoryError 异常

- 运行时常量池

	- 方法区的一部分
	- Class 文件

		- 类的版本
		- 字段
		- 方法
		- 接口

	- 特点

		- 存放编译器生成的各种字面量和符号引用
		- 具备动态性，运行期间也能将新的常量放入池中

			- String.intern 方法

- 直接内存

	- 不属于虚拟机运行时数据区的一部分，也不是内存区域
	- 依然可能导致 OutOfMemoryError 异常

		- 各个内存区域总和大于物理内存限制时

### HotSpot 虚拟机对象探秘

### 实战：OutOfMemoryError 异常

## 3.垃圾收集器与内存分配策略

### 概述

### 对象已死吗

- 引用计数算法
- 可达性分析算法
- 再谈引用
- 生存还是死亡
- 回收方法区

### 垃圾收集算法

- 标记-清除算法

	- 分为「标记」和「清除」两个阶段
	- 缺点

		- 效率不高
		- 大量的空间碎片，没有连续的内存区域，从而导致触发 Full GC

- 复制算法

	- 将可用的内存按照容量划分为大小相等的两块，每次只使用其中的一块。
	- 当一块满了，把存活的复制到另一块，然后清除该块内存
	- 所以堆区就有了 Eden 和 Survivor 的划分，默认比例 8:1

- 标记-整理算法

	- 与「标记-清除」中的标记过程一样，只是清理的时候首先让对象都像一端移动，然后清理掉端便捷以外的内存

- 分代收集算法

	- 对不同年龄代的对象采用不同的垃圾回收算法
	- 新生代使用复制算法
	- 老年代就使用标记-整理或者标记清除算法

### HotSpot 的算法实现

- 枚举根节点
- 安全点
- 安全区域

### 垃圾收集器

- Serial 收集器

	- 单线程收集器
	- 工作的时候「Stop The World」
	- 简单而高效，没有线程交互的开销，专心做垃圾收集，目前依然是 Client 模式下的默认新生代垃圾收集器

- ParNew 收集器

	- Serial 收集器多线程版本
	- 收集器的工作

		- 控制参数

			- -XX:Survivorratio
			- -XX:PretenureSizeThreshold
			- -XX:HandlePromotionFailure

		- 收集算法
		- Stop The World
		- 对象分配规则
		- 回收策略

	- 除了 Serial 收集器，只能与 CMS 收集器配合工作
	- 默认收集线程数与 CPU 数量相同，有利于有效利用系统和线程资源
	- 相关 JVM 参数

		- 默认指定 ParNew 收集器

			- -XX:+UseConcMarkSweepGC
			- -XX:+UseParNewGC

		- 限制垃圾收集的线程数

			- -XX:ParallelGCThreads

- Parallel Scavenge 收集器，「吞吐量优先」收集器

	- 新生代收集器，使用复制算法，并行多线程收集器
	- 目标是达到一个可控制的吞吐量：运营用户代码时间 / （CPU 用于运行用户代码时间 + 垃圾收集时间） 
	- 相关 JVM 参数

		- 控制最大垃圾收集停顿时间

			- -XX:MaxGCPauseMillis

		- 直接设置吞吐量大小

			- -XX:GCTimeRatio

		- 自动设置新生代堆区参数，GC 自适应调节策略

			- -XX:+UseAdaptiveSizePolicy
			- 开启这个开关就不需要手工设置新生代的大小（-Xmn）、Eden 与 Survivor 的比例、晋升老年代对象的年龄

- Serial Old 收集器

	- Serial 收集器的老年代版本
	- 使用标记整理算法
	- 两大用途

		- JDK 1.5 之前与 Parallel Scavenge 收集器搭配使用
		- 作为 CMS 收集器的后备预案

- Parallel Old 收集器

	- Parallel Scavenge 收集器的老年代版本
	- 使用标记整理算法

- CMS(Concurrent Mark Sweep，并发整理清除) 收集器

	- 目标是获取最短回收停顿时间
	- 使用标记清除算法
	- 步骤

		- 初始标记，Stop The World
		- 并发标记
		- 重新标记，Stop The World
		- 并发清除

	- 并发收集，低停顿
	- 缺点

		- 对 CPU 资源非常敏感，默认启动垃圾收集线程数：(CPU 数量 + 3)/ 4
		- 无法处理浮动垃圾从而导致 Full GC
		- 标记-清除算法固有的空间碎片问题

- G1 收集器

	- 特点

		- 并行与并发
		- 分代收集
		- 空间整合

			- 整体是标记-整理算法

		- 可预测的停顿

	- 步骤

		- 初始标记
		- 并发标记
		- 重新标记
		- 筛选回收

- 理解 GC 日志

	- GC 发生的时间，从 Java 虚拟机启动以来经过的秒数
	- 垃圾收集停顿的类型
	- GC 发生的区域
	- GC 前该内存区域已使用容量 -> GC 后该内存区域已使用容量
	- GC 前 Java 堆已使用容量 -> GC 后 Java 堆已使用堆容量
	- GC 所占用的时间（秒）

- 垃圾收集器参数总结

	- UseSerialGC

		- 虚拟机运行在 Client 模式下的默认值，打开此开关后，使用 Serial + Serial Old 的收集器组合进行内存回收

	- UseParNewGC

		- 打开此开关后，使用 ParNew + Serial Old 的收集器组合进行内存回收

	- UseConcMarkSweepGC

		- 打开此开关后，使用 ParNew + CMS + Serial Old 的收集器组合进行内存回收

	- UseParallelGC

		- 虚拟机运行在 Server 模式下的默认值，打开此开关后，使用 Parallel Scavenge + Serial Old 的收集器组合进行内存回收。

	- UseParallelOldGC

		- 打开此开关后，使用 Parallel Scavenge + Parallel Old 的收集器组合进行回收

	- SurvivorRatio

		- 新生代中 Eden 区域与 Survivor 区域的容量比值，默认为 8，代表 Eden:Survivor = 8:1

	- PretenureSizeThreshold

		- 直接晋升到来年代的对象大小，设置这个参数后，大于这个参数对象将直接在老年代分配

	- MaxTenuringThreshold

		- 晋升到老年代的对象年龄。每个对象再坚持过一次 Minor GC 之后，年龄就增加，当超过这个参数值时就进入老年代

	- UseAdaptiveSizePolicy

		- 动态调整 Java 堆中各个区域的大小以及进入老年代的年龄

	- HandlePromotionFailure

		- 是否允许分配担保失败，及老年代的剩余空间不足以应付新生代的整个 Eden 和 Survivor 区的所有对象都存活的极端情况

	- ParallelGCThreads

		- 设置并行 GC 时进行内存回收的线程数

	- GCTimeRatio

		- GC 时间占总时间的比率，默认值为 99，即允许 1% 的GC 的时间。仅在使用 Parallel Scavenge 收集器时生效

	- MaxGCPauseMillis

		- 设置 GC 的最大停顿时间。仅在使用 Parallel Scavenge 收集器时生效

	- CMSInitiatingOccupancyFraction

		- 设置 CMS 收集器在老年代空间被使用多少后触发垃圾收集，默认值 68%，仅在使用 CMS 收集器时生效

	- UseCMSCompactAtFullCollection

		- 设置 CMS 收集器在完成垃圾收集后是否要进行一次内存碎片整理，仅在使用 CMS 收集器时生效

	- CMSFullGCBeforeCompaction

		- 设置 CMS 收集器在进行若干次垃圾收集后在启动一次内存碎片整理。仅在使用 CMS 收集器时生效

### 内存分配与回收策略

- 对象优先在 Eden 分配
- 大对象直接进入老年代
- 长期存活的对象将进入老年代
- 动态对象年龄判定
- 空间分配担保

### 总结

*glorze.com - 高老四博客*