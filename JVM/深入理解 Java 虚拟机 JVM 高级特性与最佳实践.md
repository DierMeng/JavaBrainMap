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

			- 像 CMS 收集器一样，能与应用程序线程并发执行。

		- 空间整合

			- 整体是标记-整理算法

				- G1 是一个有整理内存过程的垃圾收集器，不会产生很多内存碎片。
				- G1 的 Stop The World(STW) 更可控，G1 在停顿时间上添加了预测机制，用户可以指定期望停顿时间。

		- 可预测的停顿

	- 步骤

		- 初始标记
		- 并发标记
		- 重新标记
		- 筛选回收

	- 相关概念

		- Region

			- 传统的 GC 收集器将连续的内存空间划分为新生代、老年代和永久代，特点是各代的存储地址是连续的
			- G1 的各代存储地址是不连续的，每一代都使用了 n 个不连续的大小相同的 Region，每个 Region 占有一块连续的虚拟内存地址

				- Humongous

					- 表示 Region 存储的是巨大对象，大小大于等于 region 一半的对象
					- H-obj 直接分配到了 old gen，防止了反复拷贝移动
					- H-obj 在 global concurrent marking 阶段的 cleanup 和 full GC 阶段回收
					- 为了减少连续 H-objs 分配对 GC 的影响，需要把大对象变为普通的对象，建议增大 Region size。

						- -XX:G1HeapRegionSize
						- 取值范围从 1M 到 32M，且是 2 的指数。如果不设定，那么 G1 会根据 Heap 大小自动决定

		- SATB（Snapshot-At-The-Beginning，GC 开始时活着的对象的一个快照）

			- 通过 Root Tracing 得到的，作用是维持并发 GC 的正确性。 

				- 三色标记算法

					- 黑

						- 对象被标记了，且它的所有 field 也被标记完了

					- 白

						- 对象没有被标记到，标记阶段结束后，会被当做垃圾回收掉

					- 灰

						- 对象被标记了，但是它的 field 还没有被标记或标记完

		- RSet（Remembered Set，辅助 GC 过程的一种结构，典型的空间换时间工具）

			- 集合里的 Region 可以是任意年代的

		- Pause Prediction Model（停顿预测模型）

			- 与 CMS 最大的不同是，用户可以设定整个 GC 过程的期望停顿时间

				- -XX:MaxGCPauseMillis 指定一个 G1 收集过程目标停顿时间，默认值 200ms，不过它不是硬性条件，只是期望值
				- 根据这个模型统计计算出来的历史数据来预测本次收集需要选择的 Region 数量，从而尽量满足用户设定的目标停顿时间

	- G1 的 GC 过程

		- 两种模式（完全 Stop The World 的）

			- Young GC

				- 选定所有年轻代里的 Region。通过控制年轻代的 region 个数，即年轻代内存大小，来控制 young GC 的时间开销

			- Mixed GC

				- 选定所有年轻代里的 Region，外加根据 global concurrent marking 统计得出收集收益高的若干老年代 Region

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

## 4.虚拟机性能监控与故障处理

### 监控点

- 运行日志
- 异常堆栈
- GC 日志
- 线程快照（threaddump/javacore 文件）
- 堆转储快照（heapdump/hprof 文件）

### JDK 的命令行工具

- jps：虚拟机进程状况工具

	- 显示指定系统内所有的 HotSpot 虚拟机进程
	- 列出正在运行的虚拟机进程，并显示虚拟机执行主类名称以及这些进程的本地虚拟机唯一 ID
	- jps [options] [hostid] 

		- 如果是本地 JVM，hostid 可以忽略

	- options 参数及相关功能

		- -q：只输出 LVMID，省略主类的名称
		- -m：输出虚拟机进程启动时传递给主类 main() 函数的参数
		- -l：输出主类的全名,如果进程执行的是 jar 包,输出 jar 路径
		- -v：输出虚拟机进程启动时 JVM 参数

- jstat：虚拟机统计信息监视工具

	- 用于收集 HotSpot 虚拟机各方面的运行数据
	- 显示本地或者远程虚拟机进程中的类装载、内存、垃圾收集、JIT 编译等运行数据。
	- jstat [option vmid] [interval] [s|ms] [count]

		- interval 代表查询的时间间隔
		- count 代表查询次数

	- option 参数及相关功能

		- 类装载

			- -class：监视类装载、卸载数量、总空间以及类装载所耗费的时间

				- Loaded：加载 Java 类的数量
				- Bytes：所占用空间大小
				- Unloaded：未加载数量
				- Bytes：未加载占用空间
				- Time：时间

		- 垃圾收集

			- -gc：监视 Java 堆状况，包括 Eden 区、两个 survivor 区、老年代、永久代等的容量、已用空间、GC 时间合计等信息。

				- S0C：第一个幸存区的大小
				- S1C：第二个幸存区的大小
				- S0U：第一个幸存区的使用大小
				- S1U：第二个幸存区的使用大小
				- EC：伊甸园区的大小
				- EU：伊甸园区的使用大小
				- OC：老年代大小
				- OU：老年代使用大小
				- MC：方法区大小
				- MU：方法区使用大小
				- CCSC:压缩类空间大小
				- CCSU:压缩类空间使用大小
				- YGC：年轻代垃圾回收次数
				- YGCT：年轻代垃圾回收消耗时间
				- FGC：老年代垃圾回收次数
				- FGCT：老年代垃圾回收消耗时间
				- GCT：垃圾回收消耗总时间

			- -gccapacity：监视内容与「-gc」基本相同，但输出主要关注 Java 堆各个区域使用到的最大、最小空间

				- NGCMN：新生代最小容量
				- NGCMX：新生代最大容量
				- NGC：当前新生代容量
				- S0C：第一个幸存区大小
				- S1C：第二个幸存区的大小
				- EC：伊甸园区的大小
				- OGCMN：老年代最小容量
				- OGCMX：老年代最大容量
				- OGC：当前老年代大小
				- OC:当前老年代大小
				- MCMN:最小元数据容量
				- MCMX：最大元数据容量
				- MC：当前元数据空间大小
				- CCSMN：最小压缩类空间大小
				- CCSMX：最大压缩类空间大小
				- CCSC：当前压缩类空间大小
				- YGC：年轻代gc次数
				- FGC：老年代GC次数

			- -gcutil：监视内容与「-gc」基本相同，但输出主要关注已使用空间占总空间的百分比

				- S0：幸存1区当前使用比例
				- S1：幸存2区当前使用比例
				- E：伊甸园区使用比例
				- O：老年代使用比例
				- M：元数据区使用比例
				- CCS：压缩使用比例
				- YGC：年轻代垃圾回收次数
				- FGC：老年代垃圾回收次数
				- FGCT：老年代垃圾回收消耗时间
				- GCT：垃圾回收消耗总时间

			- -gccause：与「-gcutil」功能一样，但是会额外输出导致上一次 GC 产生的原因

				- LGCC：最近垃圾回收的原因
				- GCC：当前垃圾回收的原因

			- -gcnew：监视新生代 GC 状况

				- S0C：第一个幸存区大小
				- S1C：第二个幸存区的大小
				- S0U：第一个幸存区的使用大小
				- S1U：第二个幸存区的使用大小
				- TT:对象在新生代存活的次数
				- MTT:对象在新生代存活的最大次数
				- DSS:期望的幸存区大小
				- EC：伊甸园区的大小
				- EU：伊甸园区的使用大小
				- YGC：年轻代垃圾回收次数
				- YGCT：年轻代垃圾回收消耗时间

			- -gcnewcapacity：监视内容与「-gcnew」基本相同，输出主要关注使用到的最大、最小空间

				- NGCMN：新生代最小容量
				- NGCMX：新生代最大容量
				- NGC：当前新生代容量
				- S0CMX：最大幸存1区大小
				- S0C：当前幸存1区大小
				- S1CMX：最大幸存2区大小
				- S1C：当前幸存2区大小
				- ECMX：最大伊甸园区大小
				- EC：当前伊甸园区大小
				- YGC：年轻代垃圾回收次数
				- FGC：老年代回收次数

			- -gcold：监视老年代 GC 状况

				- MC：方法区大小
				- MU：方法区使用大小
				- CCSC:压缩类空间大小
				- CCSU:压缩类空间使用大小
				- OC：老年代大小
				- OU：老年代使用大小
				- YGC：年轻代垃圾回收次数
				- FGC：老年代垃圾回收次数
				- FGCT：老年代垃圾回收消耗时间
				- GCT：垃圾回收消耗总时间

			- -gcoldcapacity：监视内容与「-gcold」基本相同,输出主要关注使用到的最大、最小空间

				- OGCMN：老年代最小容量
				- OGCMX：老年代最大容量
				- OGC：当前老年代大小
				- OC：老年代大小
				- YGC：年轻代垃圾回收次数
				- FGC：老年代垃圾回收次数
				- FGCT：老年代垃圾回收消耗时间
				- GCT：垃圾回收消耗总时间

			- -gcpermcapacity/-gcmetacapacity：输出永久代使用到的最大、最小空间（JDK8 之后已经永久代已经改为元空间，没有该参数，随之代替的是「-gcmetacapacity」参数）

				- MCMN: 最小元数据容量
				- MCMX：最大元数据容量
				- MC：当前元数据空间大小
				- CCSMN：最小压缩类空间大小
				- CCSMX：最大压缩类空间大小
				- CCSC：当前压缩类空间大小
				- YGC：年轻代垃圾回收次数
				- FGC：老年代垃圾回收次数
				- FGCT：老年代垃圾回收消耗时间
				- GCT：垃圾回收消耗总时间

		- 运行期编译状况

			- -compilier：输出 JIT 编译器编译过的方法、耗时等信息

				- Compiled：编译数量
				- Failed：失败数量
				- Invalid：不可用数量
				- Time：时间
				- FailedType：失败类型
				- FailedMethod：失败的方法

			- -printcompilation：输出已经被 JIT 编译的方法

				- Compiled：最近编译方法的数量
				- Size：最近编译方法的字节码数量
				- Type：最近编译方法的编译类型。
				- Method：方法名标识。

- jinfo：Java 配置信息工具

	- 显示虚拟机配置信息
	- 实时地查看和调整虚拟机各项参数
	- jinfo [option] pid

		- jinfo -flag：查询参数配置

			- jinfo -flag CMSInittiatingOccupancyFaction 9537

		- java -XX:PrintFlagsFinal（查看参数默认值，列表形式）
		- jinfo -sysprops：把虚拟机进程的 System.getProperties() 的内容打印出来。

- jmap：Java 内存映像工具

	- 生成虚拟机的内存转储快照（heapdump 文件）
	- -XX:+HeapDumpOnOutOfMemoryError 参数也可以让虚拟机在 OOM 异常出现之后自动生成 dump 文件
	- Linux 下也可以通过 kill -3 命令发送进程退出信号「吓唬」虚拟机拿到 dump 文件
	- jmap [option] vmid

		- -dump：生成 Java 堆转储快照。
格式：-dump[live,]format=b,file=<filename>，其中 live 代表是否只 dump 出存活对象。
		- -finalizerinfo：显示在 F-Queue 中等待 Finalizer 线程执行 finalize 方法的对象。在 Linux 下有效
		- -heap：显示 Java 堆详细信息

			- 使用哪种回收器
			- 参数配置
			- 分代状况
			-            各参数代表的含义

		- -histo：显示堆中对象统计信息

			- 类、实例数量
			- 合集容量

		- -permstat：以 ClassLoader 为统计口径显示永久代内存状态，Linux 下有效，JDK8 之后改为「-clstats」选项。
		- -F：当虚拟机进程对 -dump 选项没有响应时，可使用这个选项强制生成 dump 快照。在 Linux 下有效。

- jhat：虚拟机堆转储快照分析工具

	- 用于分析 heapdump 文件，它会建立一个 HTTP/HTML 服务器，让用户可以在浏览器上查看分析结果
	- 一般不用

		- 一般不会在部署应用程序的服务器上直接分析 dump 文件，分析工作是一个耗时而且消耗硬件资源的过程。
		- jhat 的分析功能相对来说比较简陋

- jstack：Java 堆栈跟踪工具

	- 显示虚拟机的线程快照（threaddump 或者 javacore 文件）
	- 就是当前虚拟机内每一条线程正在执行的方法的堆栈的集合，目的是定位线程出现长时间停顿的原因

		- 线程死锁
		- 死循环
		- 请求外部资源导致的长时间等待

	- jstack [option] vmid

		- -F：当正常输出的请求不被响应时，强制输出线程堆栈
		- -l：除堆栈外，显示关于锁的附加信息
		- -m：如果调用到本地方法的话，可以显示 C/C++ 的堆栈

- hsdis：jit 生成代码反汇编

### JDK 的可视化工具

- JConsole
- VisualVM

