# JVM 实用参数（基于 Java 6 的 HotSpot JVM）

## JVM 类型以及编译器模式

### -server & -client

- 有两种类型的 HotSpot JVM，一般我们开发默认安装都是 server 端。类比起来，server 端更像是 JDK，client 更像是 JRE，client 端占用更少的内存和启动时间，但是不像服务端的 VM 会为堆空间提供更大的空间和并行的垃圾收集器
- java -server

	- 指定虚拟机为服务端，一般我们安装默认就是这个，也推荐使用服务端 VM
	- JVM功效学：在 JVM 启动的时候根据可用的硬件和操作系统来自动的选择JVM的类型。

- java -client

	- 在一个 32 位的系统上，HotSpot JDK 可以运行服务端 VM，但是 32 位的 JRE 只能运行客户端 VM。

### -version and -showversion

- 查看安装的 Java 版本和 JVM 类型

	- mixed mode，混合模式，HotSpot 默认的运行模式

		- JVM在运行时可以动态的把字节码编译为本地代码。

	- class data sharing，类数据共享

		- 一种在只读缓存（在 jsa 文件中，Java Shared Archive）中存储 JRE 的系统类，被所有 Java 进程的类加载器用来当做共享资源。类数据共享（Class data sharing）可能在经常从 jar 文档中读所有的类数据的情况下显示出性能优势。

- java -version

	- 参数在打印完上述信息后立即终止 JVM

- java -showversion

	- 输出相同的信息，但是 -showversion 紧接着会处理并执行 Java 程序。

### -Xint & -Xcomp & -Xmixed

- java -Xint

	- 解释模式（interpreted mode）下，-Xint 标记会强制 JVM 执行所有的字节码，这会降低运行速度，通常低 10 倍或更多。

- java -Xcomp

	- 与 -Xint 正好相反，JVM 在第一次使用时会把所有的字节码编译成本地代码，从而带来最大程度的优化。但是 -xcomp 没有让 JVM 启用 JIT 编译器的全部功能，所以很多应用在使用 -Xcomp 也会有一些性能上的损失。

- java -Xmixed

	- 混合模式 -Xmixed，最新版本的 HotSpot 的默认模式就是混合模式，运行时间长的应用，建议使用 JVM 的默认设置,让 JIT 编译器充分发挥其动态潜力，JIT 编译器是组成 JVM 最重要的组件之一，也正是因为 JVM 在这方面的进展才让 Java 不再那么慢。

## 参数分类和即时（JIT）编译器诊断

### 参数分类

- 标准参数

	- 功能和输出的参数都是很稳定的，在将来的 JVM 版本中也基本不会改变。可以用 java -help 检索出所有标准参数。

- X 参数

	- 非标准化的参数，意思就是在将来的版本中可能会改变。所有的这类参数都以 -X 开始，可以用 java -X 来检索，但是不能保证所有参数都可以被检索出来。

- XX 参数，「-XX:」

	- XX参数，也是不标准的，X 参数的功能是十分稳定的，而 XX 参数是属于仍在实验当中的。XX 参数不应该在不了解的情况下使用。
	- 语法

		- 对于布尔类型的参数，有「+」或「-」，然后才设置 JVM 选项的实际名称。例如，-XX:+<name> 用于激活 <name> 选项，而 -XX:-<name> 用于注销选项。
		- 对于需要非布尔值的参数，如 String 或者 Integer，先写参数的名称，后面加上「=」，最后赋值。例如，  -XX:<name>=<value> 给 <name> 赋值 <value>。

### JIT 编译方面的一些 XX 参数

- java -XX:+PrintCompilation

	- 输出一些关于从字节码转化成本地代码的编译过程。

- java -XX:+CITime

	- 可以在 JVM 关闭时得到各种编译的统计信息。

- java -XX:+UnlockExperimentalVMOptions

	- 当设置一个特定的 JVM 参数时，JVM 会在输出「Unrecognized VM option」后终止。如果发生了这种情况，应该首先检查是否输错了参数。然而，如果参数输入是正确的，并且 JVM 并不识别，或许就需要设置 -XX:+UnlockExperimentalVMOptions 来解锁参数。

- -XX:+LogCompilation

	- 把扩展的编译输出写到「hotspot.log」文件中，但是需要使用 -XX:+UnlockExperimentalVMOptions 来解锁。

- -XX:+PrintOptoAssembly

	- 把编译器线程生成的本地代码被输出并写到「hotspot.log」文件中，使用这个参数要求运行的服务端 VM 是 debug 版本。

## 打印所有XX参数及值

### java -XX:+PrintFlagsFinal

- 表格的每一行包括五列，来表示一个 XX 参数。第一列表示参数的数据类型，第二列是名称，第四列为值，第五列是参数的类别。第三列「=」表示第四列是参数的默认值，而「:=」表明了参数被用户或者 JVM 赋值了。

### java -XX:+PrintFlagsInitial

- 查看所有参数的默认值

### 查询已经被赋值「:=」的参数

- java -server -XX:+UnlockExperimentalVMOptions -XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal Java类名称 | grep ":"

### java -XX:+PrintCommandLineFlags

- 让 JVM 打印出那些已经被用户或者 JVM 设置过的详细的 XX 参数的名称和值。建议 –XX:+PrintCommandLineFlags 这个参数应该总是设置在JVM启动的配置项里。

## 内存调优

### 堆内存划分

- 新生代（young generation）：存储着新分配的和较年轻的对象
- 老年代（old generation）：存储着长寿的对象
- 永久代（permanent generation）：存储着那些需要伴随整个 JVM 生命周期的对象。比如，已加载的对象的类定义或者 String 对象内部缓存
- G1 垃圾回收器：模糊了新生代和老年代之间的区别

### -Xms and -Xmx (or: -XX:InitialHeapSize & -XX:MaxHeapSize)

- -Xms

	- 初始堆内存

- -Xmx

	- 最大堆内存

### -XX:+HeapDumpOnOutOfMemoryError & -XX:HeapDumpPath

- 分析堆内存快照（Heap Dump）是一个很好的定位内存溢出问题手段。堆内存快照会默认保存在 JVM 的启动目录下名为 java_pid<pid>.hprof 的文件里（<pid> 就是 JVM 进程的进程号）
- -XX:+HeapDumpOnOutOfMemoryError

	- 让 JVM 在发生内存溢出时自动的生成堆内存快照。

- -XX:HeapDumpPath=<path>

	- 改变默认的堆内存快照生成路径，可以相对路径也可以是绝对路径

### -XX:OnOutOfMemoryError

- 接受一串指令和它们的参数，当发生内存溢出的时候，可以执行一些指令来达到清理或通知的监控行为。
- 示例：当内存溢出错误发生的时候，我们会将堆内存快照写到 /tmp/heapdump.hprof 文件并且在 JVM 的运行目录执行脚本 cleanup.sh

  java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump.hprof -XX:OnOutOfMemoryError ="sh ~/cleanup.sh" 项目、工程名称

### -XX:PermSize & -XX:MaxPermSize

- 永久代在堆内存中是一块独立的区域，它包含了所有 JVM 加载的类的对象表示。
- -XX:MaxPermSize

	- 设置永久代大小的最大值

- -XX:PermSize

	- 设置永久代初始大小

### -XX:InitialCodeCacheSize & -XX:ReservedCodeCacheSize

- 代码缓存：存储已编译方法生成的本地代码。

  如果代码缓存被占满，JVM 会打印出一条警告消息，并切换到 interpreted-only 模式（解释模式）：JIT 编译器被停用，字节码将不再会被编译成机器码。

- 字节值

### -XX:+UseCodeCacheFlushing

- 当代码缓存被填满时让 JVM 放弃一些编译代码，通过使用 -XX:+UseCodeCacheFlushing  这个参数，可以避免当代码缓存被填满的时候 JVM 切换到 interpreted-only 模式（解释模式）。

## 额外杂乱未分类总结

### 调整最小堆容量个最大堆容量

- -Xms256M

	- ms 全称 memory start，代表最小堆容量

- -Xmx256M

	- mx 全称 memory max，代表最大堆容量

- -X 表示它是 JVM 运行参数
- 线上环境中，一般都是将 JVM 的 Xms 和 Xmx 设置成一样的大小，避免在 GC 后调整堆大小时带来的额外压力

### 配置计数器的值到达某个阈值的时候，对象从新生代晋升至老年代

- -XX:MaxTenuringThreshold
- 默认设置为 15，如果配置为 1，则直接晋升至老年代 

### 让 JVM 遇到 OOM 异常时能输出堆内信息

- -XX:HeapDumpOnOutOfMemoryError

### 设置永久代空间

- -XX:MaxPermSize=1280m

### 启用 G1 垃圾收集器

- -XX:UseG1GC

### 收集器相关参数

- -XX:Survivorratio
- -XX:PretenureSizeThreshold
- -XX:HandlePromotionFailure

### 默认指定 ParNew 收集器

- -XX:+UseConcMarkSweepGC
- -XX:+UseParNewGC

### ParNew 收集器相关 JVM 参数

- 默认指定 ParNew 收集器

	- -XX:+UseConcMarkSweepGC
	- -XX:+UseParNewGC

- 限制垃圾收集的线程数

	- -XX:ParallelGCThreads

### 设置新生代空间的大小

- -Xmn

### 设置 Eden 与 Survivor 的比例

- -XX:SurvivorRatio

### Parallel Scavenge 收集器相关 JVM 参数

- 控制最大垃圾收集停顿时间

	- -XX:MaxGCPauseMillis

- 直接设置吞吐量大小

	- -XX:GCTimeRatio

- 自动设置新生代栈帧参数，GC 自适应调节策略

	- -XX:+UseAdaptiveSizePolicy
	- 开启这个开关就不需要手工设置新生代的大小（-Xmn）、Eden 与 Survivor 的比例、晋升老年代对象的年龄

### 查看 GC 日志

- -XX:PrintGCDetails

### 在 Serial 和 ParNew GC 回收期中，晋升年龄阈值通过参数「MaxTenuringThreshold」设定

### Java GC 日志可以通过 +PrintGCDetails开启

### CMS 为了避免这个阶段没有等到 Minor GC 而陷入无限等待，提供了参数「CMSMaxAbortablePrecleanTime」，默认为 5s，含义是如果可中断的预清理执行超过5s，不管发没发生 Minor GC，都会中止此阶段，进入重新标记阶段。

### CMS 提供 CMSScavengeBeforeRemark 参数，用来保证重新标记阶段之前强制进行一次 Minor GC。

### 在启动时观察加载了哪个 Jar 包中的哪个类

- -XX:_TraceClassLoading

## 新生代垃圾回收

对象一般出生在 Eden 区，年轻代 GC 过程中，对象在 2 个幸存区之间移动，如果对象存活到适当的年龄，会被移动到老年代。当对象在老年代死亡时，就需要更高级别的 GC，更重量级的 GC 算法。

### 把堆划分为新生代和老年代的好处

- 简化了新对象的分配（只在新生代分配内存）
- 可以更有效的清除不再需要的对象
- 新生代和老年代使用不同的 GC 算法

	- 新生代使用复制算法

### 新生代

- 伊甸园区（Eden）

	- 新对象会首先分配在 Eden 中
	- 如果新对象过大，会直接分配在老年代中

- From 幸存区（survivor）
- To 幸存区（survivor）

	- GC 前保持清空
	- GC 运行时,Eden 中的幸存对象被复制到这里

### -XX:NewSize &-XX:MaxNewSize

-  -XX:NewSize

	- 设置新生代初始大小

-  -XX:MaxNewSize

	- 新生代设置的越大，老年代区域就会减少。一般不允许新生代比老年代还大，最大可以设置为 -Xmx/2。

### -XX:NewRatio

- 设置老年代与新生代的比例
- -XX:NewRatio=3 指定老年代/新生代为3/1。老年代占堆大小的 3/4 ，新生代占 1/4。
- 设置新生代和老年代的相对大小的这种方式优点是「新生代大小会随着整个堆大小动态扩展」

### -XX:SurvivorRatio

- 指定伊甸园区（Eden）与幸存区大小比例
- -XX:SurvivorRatio=10 表示伊甸园区（Eden）是幸存区 To 大小的 10 倍（也是幸存区 From 的 10 倍）
- 目标：最小化短命对象晋升到老年代的数量，也最小化新生代 GC 的次数和持续时间。

### -XX:+PrintTenuringDistribution

- 指定 JVM 在每次新生代 GC 时，输出幸存区中对象的年龄分布。
- 老年代阀值：意思是对象从新生代移动到老年代之前，经过几次 GC（即对象晋升前的最大年龄）

### -XX:InitialTenuringThreshold、-XX:MaxTenuringThreshold & -XX:TargetSurvivorRatio

- -XX:InitialTenuringThreshold

	- 设定老年代阀值的初始值

- -XX:MaxTenuringThreshold

	- 设定老年代阀值的最大值

- -XX:TargetSurvivorRatio

	- 设定幸存区的目标使用率

- 常见场景

	- 如果从年龄分布中发现，有很多对象的年龄持续增长，在到达老年代阀值之前。这表示 -XX:MaxTenuringThreshold 设置过大。
	- 如果 -XX:MaxTenuringThreshold 的值大于 1，但是很多对象年龄从未大于 1。应该看下幸存区的目标使用率。如果幸存区使用率从未到达，这表示对象都被 GC 回收，这正是我们想要的。 如果幸存区使用率经常达到，有些年龄超过 1 的对象被移动到老年代中。这种情况，可以尝试调整幸存区大小或目标使用率。

### -XX:+NeverTenure and -XX:+AlwaysTenure

-  -XX:+NeverTenure

	- 对象永远不会晋升到老年代，但是这种情况至少会浪费至少一半的堆内存

- -XX:+AlwaysTenure

	- 没有幸存区，所有对象在第一次 GC 时，会晋升到老年代。

## 吞吐量收集器

### 评估一个垃圾收集（GC）算法好坏的标准

- 吞吐量（throughput）越高算法越好

  应用程序线程用时占程序总用时的比例。

- 暂停时间（pause times）越短算法越好

  一个时间段内应用程序线程让 GC 线程执行而完全暂停的平均时间

### 老年代垃圾收集算法

- 第一类算法试图最大限度地提高吞吐量，停止-复制停止-复制算法，Parallel Old 收集器
- 第二类算法试图最小化暂停时间，标记-清除算法，CMS
- G1 垃圾收集算法

### 面向吞吐量垃圾收集算法有关的重要JVM配置参数

- -XX:+UseSerialGC

	- 激活串行垃圾收集器，例如单线程面向吞吐量垃圾收集器。推荐用于只有单个可用处理器核心的 JVM。

- -XX:+UseParallelGC

	- 告诉 JVM 使用多线程并行执行年轻代垃圾收集。

- -XX:+UseParallelOldGC

	- 「old」实际上是指年老代，所以 -XX:+UseParallelOldGC 要优于 -XX:+UseParallelGC：除了激活年轻代并行垃圾收集，也激活了年老代并行垃圾收集。 当期望高吞吐量，并且JVM有两个或更多可用处理器核心时，建议使用。

- -XX:ParallelGCThreads

	- 指定并行垃圾收集的线程数量

	  java -XX:ParallelGCThreads=6

- -XX:-UseAdaptiveSizePolicy

	- 停用一些人体工程学

	  通过人体工程学，垃圾收集器能将堆大小动态变动像 GC 设置一样应用到不同的堆区域，只要有证据表明这些变动将能提高 GC 性能。

- -XX:GCTimeRatio

	- 告诉 JVM 吞吐量要达到的目标值，数学角度就是 -XX:GCTimeRatio=N 指定了目标应用程序线程的执行时间（与总的程序执行时间）达到 N/(N+1) 的目标比值。默认值 99 

	  通过 -XX:GCTimeRatio=9 我们要求应用程序线程在整个执行时间中至少 9/10 是活动的（因此，GC 线程占用其余 1/10）。

- -XX:MaxGCPauseMillis

	- 告诉 JVM 最大暂停时间的目标值（以毫秒为单位）。

## CMS 收集器

### 主要目标：低应用停顿时间

### CMS 收集器的 GC 周期

- 初始标记

	- 为了收集应用程序的对象引用需要暂停应用程序线程，该阶段完成后，应用程序线程再次启动。

- 并发标记

	- 从第一阶段收集到的对象引用开始，遍历所有其他的对象引用。

- 并发预清理

	- 改变当运行第二阶段时，由应用程序线程产生的对象引用，以更新第二阶段的结果。

- 重标记

	- 由于第三阶段是并发的，对象引用可能会发生进一步改变。因此，应用程序线程会再一次被暂停以更新这些变化，并且在进行实际的清理之前确保一个正确的对象引用视图。这一阶段十分重要，因为必须避免收集到仍被引用的对象。

- 并发清理

	- 所有不再被应用的对象将从堆里清除掉。

- 并发重置

	- 收集器做一些收尾的工作，以便下一次 GC 周期能有一个干净的状态。

### CMS 收集器的问题

- 堆碎片

	- 没有足够连续的空间完全容纳对象

- 对象分配率高

	- 并发模式失败：如果获取对象实例的频率高于收集器清除堆里死对象的频率，并发算法将再次失败。从某种程度上说，老年代将没有足够的可用空间来容纳一个从年轻代提升过来的对象。

### -XX：+UseConcMarkSweepGC

- 激活 CMS 收集器，HotSpot JVM 默认使用的就是并行收集器。

### -XX：UseParNewGC

- 激活年轻代使用多线程并行执行垃圾回收，当使用 -XX：+UseConcMarkSweepGC 时，-XX：UseParNewGC会自动开启。

### -XX：+CMSConcurrentMTEnabled

- 并发的 CMS 阶段将以多线程执行（也就是多个 GC 线程会与所有的应用程序线程并行工作），默认开启。

### -XX：ConcGCThreads

- 定义并发 CMS 过程运行时的线程数，默认值：ConcGCThreads = (ParallelGCThreads + 3)/4

### -XX:CMSInitiatingOccupancyFraction

- 代表老年代堆空间的使用率
- -XX:CMSInitiatingOccupancyFraction==75 意味着第一次 CMS 垃圾收集会在老年代被占用 75% 时被触发，CMSInitiatingOccupancyFraction 的默认值为 68。

### -XX：+UseCMSInitiatingOccupancyOnly

- 命令 JVM 不基于运行时收集的数据来启动 CMS 垃圾收集周期，当该标志被开启时，JVM 通过 CMSInitiatingOccupancyFraction 的值进行每一次 CMS 收集，而不仅仅是第一次。

### -XX:+CMSClassUnloadingEnabled

- CMS 收集器默认不会对永久代进行垃圾回收，该参数是实现对永久代进行垃圾回收。但是永久代耗尽空间 JVM 还是会尝试进行垃圾回收

### -XX:+CMSIncrementalMode

- 开启 CMS 收集器的增量模式。
- 增量模式：incremental mode，
其实就是进行并发标记、清理时让 GC 线程、用户线程交替运行，尽量减少 GC 线程独占 CPU 资源的时间。

  其实这种模式没什么太大的优化，况且增量模式经常暂停 CMS 过程，以便对应用程序线程作出完全的让步。因此，收集器将花更长的时间完成整个收集周期。因此，只有通过测试后发现正常 CMS 周期对应用程序线程干扰太大时，才应该使用增量模式。

### -XX:+ExplicitGCInvokesConcurrent & -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses

- -XX:+ExplicitGCInvokesConcurrent

	- 命令 JVM 无论什么时候调用系统 GC，都执行 CMS GC，而不是 Full GC。

- -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses

	- 保证当有系统 GC 调用时，永久代也被包括进 CMS 垃圾回收的范围内。

### -XX:+DisableExplicitGC

- 告诉 JVM 完全忽略系统的 GC 调用（不管使用的收集器是什么类型）

### -XX:+UseCMSCompactAtFullCollection

- 设置 CMS 收集器在完成垃圾收集后是否要进行一次内存碎片整理

### -XX:+CMSParallelRemarkEnabled

- 降低标记停顿，为了减少第二次暂停的时间，开启并行 remark

### -XX:CMSFullGCsBeforeCompaction

- 在上一次 CMS 并发 GC 执行过后，到底还要再执行多少次 full GC 才会做压缩，默认是 0。
- 比如 CMSFullGCsBeforeCompaction 配置为 10，就是每隔 10 次真正的 full GC 才做一次压缩，注意：不是每 10 次 CMS 并发 GC 就做一次压缩

## GC日志

准确记录了每一次的 GC 的执行时间和执行结果，通过分析 GC 日志可以优化堆设置和 GC 设置，或者改进应用程序的对象分配模式。

### -XX:+PrintGC OR -verbose:gc

- 开启简单的 GC 日志模式，为每一次新生代（young generation）的 GC 和每一次的 Full GC 打印一行信息。

### -XX:PrintGCDetails

- 开启详细的 GC 日志模式

### -XX:+PrintGCTimeStamps和-XX:+PrintGCDateStamps

- 将时间和日期也加到 GC 日志中，表示自 JVM 启动至今的时间戳也会被添加到每一行中。

### -Xloggc

- 输出到指定的文件

### 可管理的JVM参数

- 对于这些参数，可以在运行时修改他们的值。
- 「PrintGC」开头的参数都是可管理的参数，任何时候都可以开启或是关闭 GC 日志。

## JVM 垃圾收集器参数总结

### UseSerialGC

- 虚拟机运行在 Client 模式下的默认值，打开此开关后，使用 Serial + Serial Old 的收集器组合进行内存回收

### UseParNewGC

- 打开此开关后，使用 ParNew + Serial Old 的收集器组合进行内存回收

### UseConcMarkSweepGC

- 打开此开关后，使用 ParNew + CMS + Serial Old 的收集器组合进行内存回收

### UseParallelGC

- 虚拟机运行在 Server 模式下的默认值，打开此开关后，使用 Parallel Scavenge + Serial Old 的收集器组合进行内存回收。

### UseParallelOldGC

- 打开此开关后，使用 Parallel Scavenge + Parallel Old 的收集器组合进行回收

### SurvivorRatio

- 新生代中 Eden 区域与 Survivor 区域的容量比值，默认为 8，代表 Eden:Survivor = 8:1

### PretenureSizeThreshold

- 直接晋升到来年代的对象大小，设置这个参数后，大于这个参数对象将直接在老年代分配

### MaxTenuringThreshold

- 晋升到老年代的对象年龄。每个对象再坚持过一次 Minor GC 之后，年龄就增加，当超过这个参数值时就进入老年代

### UseAdaptiveSizePolicy

- 动态调整 Java 堆中各个区域的大小以及进入老年代的年龄

### HandlePromotionFailure

- 是否允许分配担保失败，及老年代的剩余空间不足以应付新生代的整个 Eden 和 Survivor 区的所有对象都存活的极端情况

### ParallelGCThreads

- 设置并行 GC 时进行内存回收的线程数

### GCTimeRatio

- GC 时间占总时间的比率，默认值为 99，即允许 1% 的GC 的时间。仅在使用 Parallel Scavenge 收集器时生效

### MaxGCPauseMillis

- 设置 GC 的最大停顿时间。仅在使用 Parallel Scavenge 收集器时生效

### CMSInitiatingOccupancyFraction

- 设置 CMS 收集器在老年代空间被使用多少后触发垃圾收集，默认值 68%，仅在使用 CMS 收集器时生效

### UseCMSCompactAtFullCollection

- 设置 CMS 收集器在完成垃圾收集后是否要进行一次内存碎片整理，仅在使用 CMS 收集器时生效

### CMSFullGCBeforeCompaction

- 设置 CMS 收集器在进行若干次垃圾收集后在启动一次内存碎片整理。仅在使用 CMS 收集器时生效

*glorze.com - 高老四博客*