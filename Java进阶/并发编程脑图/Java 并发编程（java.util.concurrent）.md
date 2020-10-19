# Java 并发编程（java.util.concurrent）

## 并发工具类

### CountDownLatch，计数器工具类

- 初始时需要指定一个计数器的大小，然后可被多个线程并发的实现减 1 操作，并在计数器为 0 后调用 await 方法的线程被唤醒，从而实现多线程间的协作。
- 任务分为 N 个子线程去执行，state 也初始化为 N（注意 N 要与线程个数一致）。
- 这 N 个子线程是并行执行的，每个子线程执行完后 countDown() 一次，state 会 CAS 减 1。
- 等到所有子线程都执行完后(即 state=0)，会 unpark(线程唤醒) 主调用线程，然后主调用线程就会从 await() 函数返回，继续后余动作。
- 实现 AQS 的共享 API

### CyclicBarrier，循环屏障式计数器

- 可循环使用（Cyclic）的屏障（Barrier），通过它可以实现让一组线程到达一个屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，所有被屏障拦截的线程才会继续执行。
- 与 CountDownLatch 的区别

	- CountDownLatch 计数为 0 时，无法重置，而 CyclicBarrier 可循环，计数到 0 时重新置为传入的值重新开始
	- CountDownLatch 的 await() 只阻塞线程不影响计数，而 CyclicBarrier 调用 await() 计数减一，-1 后如果值不为 0，线程继续阻塞
	- CountDownLatch 不可重复使用，而 CyclicBarrier 可重复使用

### Semaphore，信号量

- 用来控制同时访问特定资源的线程数量，它通过协调各个线程，以保证合理的使用公共资源。
- 类似于 CountDownLatch，资源总量 state=permits，当 state>0 时就能获得锁，并将 state减 1。
- 当 state=0 时只能等待其他线程释放锁，当释放锁时 state 加 1，其他等待线程又能获得这个锁。
- 当 Semaphore 的 permits 定义为 1 时，就是互斥锁，当 permits > 1 就是共享锁。

### Executors，线程池静态工厂

- newFixedThreadPool

	- 固定线程数，既是核心线程数也是最大线程数，不存在空闲线程

- newWorkStealingPool

	- 创建持有足够线程的线程池支持给定的并行度，并通过使用多个队列减少竞争
	- CPU 数量会被设置为默认的并行度

- newSingleThreadExecutor

	- 创建一个单线程的线程池，相当于但线程串行执行所有任务，保证按任务的提交顺序依次执行

- newCachedThreadPool

	- 高度可伸缩的线程池
	- 工作线程处于空闲状态，则回收工作线程
	- 如果任务数增加，再次创建出新线程处理任务

- newScheduledThreadPool

	- 定时及周期性任务执行
	- 相比 Timer，ScheduledExecutorService 更安全，功能更强大
	- 特点是不回收线程，而 newCachedThreadPool 回收工作线程

### Exchanger，交换者，线程间协作的工具类

- 提供一个同步点，在这个同步点两个线程可以交换彼此的数据。
- 这两个线程通过 exchange 方法交换数据， 如果第一个线程先执行exchange方法，它会一直等待第二个线程也执行exchange
- 当两个线程都到达同步点时，这两个线程就可以交换数据，将本线程生产出来的数据传递给对方。

## concurrent

### Executor 框架，线程池

- Future（接口）

	- 代表异步计算的结果，通过 Future 接口提供的方法可以查看异步计算是否执行完成，或者等待执行结果并获取执行结果，同时还可以取消执行。
	- RunnableFuture

		- RunnableFuture 继承了 Runnable 接口和 Future 接口
		- RunnableScheduledFuture
		- FutureTask

			- 事实上，FutureTask 是 Future 接口的一个唯一实现类。

				- FutureTask(Callable<V> callable)
				- FutureTask(Runnable runnable, V result)

			- 将一个 Callable 置为 FutureTask 的内置成员
			- 执行 Callable 中的 call 方法
			- 调用 futureTask.get(timeout, TimeUnit) 方法, 获取 call 的执行结果, 超时的话就报 TimeoutException

	- ScheduledFuture
	- ForkJoinTask
	- CompletableFuture

		- 在异步任务完成后，使用任务结果时就不需要等待，可以直接通过 thenAccept、thenApply、thenCompose 等方法将前面异步处理的结果交给另外一个异步事件处理线程来处理

	- 声明的方法

		- cancel(boolean mayInterruptIfRunning);

			- 用来取消异步任务的执行。
			- 如果异步任务已经完成或者已经被取消，或者由于某些原因不能取消，则会返回 false。
			- 如果任务还没有被执行，则会返回 true 并且异步任务不会被执行。
			- 如果任务已经开始执行了但是还没有执行完成，若 mayInterruptIfRunning 为 true，则会立即中断执行任务的线程并返回 true
			- 若 mayInterruptIfRunning 为 false，则会返回 true 且不会中断任务执行线程。

		- isCancelled();

			- 判断任务是否被取消，如果任务在结束（正常执行结束或者执行异常结束）前被取消则返回 true，否则返回 false。

		- isDone();

			- 任务执行过程中发生异常、任务被取消也属于任务已完成，也会返回 true。
			- 返回计算是否完成 , 若任务完成则返回 true (任务完成 state = narmal, exception, interrupted)

		- V get()

			- 获取计算的结果, 若计算没完成, 直接 await, 直到计算结束或线程中断
			- 获取任务执行结果，如果任务还没完成则会阻塞等待直到任务执行完成。
			- 如果任务被取消则会抛出 CancellationException 异常，如果任务执行过程发生异常则会抛出 ExecutionException 异常
			- 如果阻塞等待过程中被中断则会抛出 InterruptedException 异常。

		- V get(long timeout, TimeUnit unit)

			- 获取计算的结果, 若计算没完成, 直接 await, 直到计算结束或线程中断或 time 时间超时

- Callable

	- 是个泛型接口，泛型 V 就是要 call() 方法返回的类型。
	- Callable 接口和 Runnable 接口很像，都可以被另外一个线程执行，但是 Runnable 不会返回数据也不能抛出异常。
	- Callable 一般是和 ExecutorService 配合来使用的

- Executor，接口，只有一个「void execute(Runnable command);」方法 

	- ExecutorService，接口，定义完整的线程池行为

		- AbstractExecutorService，抽象类，主要是实现 ExecutorService 接口生命的任务提交等方法

			- ThreadPoolExecutor，创建一个线程池

				- 构造方法参数

					- corePoolSize（常驻核心线程数）

						- 如果当前运行的线程少于 corePoolSize（常驻核心线程数），则创建新线程来执行任务（注意，执行这一步骤需要获取全局锁）

					- runnableTaskQueue

						- 用于保存等待执行的任务的阻塞队列

							- ArrayBlockingQueue：是一个基于数组结构的有界阻塞队列，此队列按 FIFO（先进先出）原则对元素进行排序。
							- LinkedBlockingQueue：一个基于链表结构的阻塞队列，此队列按 FIFO （先进先出） 排序元素，吞吐量通常要高于 ArrayBlockingQueue。静态工厂方法 Executors.newFixedThreadPool() 使用了这个队列。
							- SynchronousQueue：一个不存储元素的阻塞队列。每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于 LinkedBlockingQueue，静态工厂方法 Executors.newCachedThreadPool 使用了这个队列。
							- PriorityBlockingQueue：一个具有优先级的无限阻塞队列。

					- maximumPoolSize

						- 如果队列已满，则创建新的线程来处理任务（注意，执行这一步骤需要获取全局锁）

					- ThreadFactory

						- 用于设置创建线程的工厂，可以通过线程工厂给每个创建出来的线程设置更有意义的名字

					- RejectedExecutionHandler

						- 当队列和线程池都满了，说明线程池处于饱和状态，那么必须采取一种策略处理提交的新任务。

							- AbortPolicy：直接抛出异常。
							- DiscardPolicy：不处理，丢弃掉。
							- DiscardOldestPolicy：丢弃队列里最近的一个任务，并执行当前任务。
							- CallerRunsPolicy：只用调用者所在线程来运行任务。
							- 自定义实现 RejectedExecutionHandler 接口

					- keepAliveTime

						- 线程池的工作线程空闲后，保持存活的时间。

				- 任务提交

					- 使用 execute 提交的任务（Runnable），但是 execute 方法没有返回值，所以无法判断任务知否被线程池执行成功
					- 使用 submit 方法来提交任务，它会返回一个 Future，可以通过这个 Future 来判断任务是否执行成功，通过 Future 的 get 方法来获取返回值，get 方法会阻塞住直到任务完成

				- 线程池的关闭

					- shutdown

						- 只是将线程池的状态设置成SHUTDOWN状态，然后中断所有没有正在执行任务的线程。

					- shutdownNow

						- 遍历线程池中的工作线程，然后逐个调用线程的interrupt方法来中断线程，所以无法响应中断的任务可能永远无法终止。

				- 线程池数据监控

					- taskCount：线程池需要执行的任务数量。
					- completedTaskCount：线程池在运行过程中已完成的任务数量。小于或等于taskCount。
					- largestPoolSize：线程池曾经创建过的最大线程数量。通过这个数据可以知道线程池是否满过。如等于线程池的最大大小，则表示线程池曾经满了。
					- getPoolSize:线程池的线程数量。如果线程池不销毁的话，池里的线程不会自动销毁，所以这个大小只增不减。
					- getActiveCount：获取活动的线程数。

			- ForkJoinPool，分而治之，工作窃取

			  老四之前在博客中写过关于 Fork/Join 框架的一片文章，可以参考《浅析Java中的Fork和Join并发编程框架》。http://www.glorze.com/792.html

				- 关于 Fork/Join 框架，老四单独总结了脑图，请在当前目录查看。

			- Executors 的静态内部类：DelegatedExecutorService

				- 包装类，方法代理到内部的 ExecutorService，只暴漏ExecutorService 定义的方法。

		- ScheduledExecutorService，接口

			- Executors 的静态内部类：ScheduledThreadPoolExecutor
			- Executors 的静态内部类：DelegatedScheduledExecutorService

		- 声明的方法

			- void shutdown();

				- 不会立即关闭，但是它不再接收新的任务，直到当前所有线程执行完成才会关闭，所有在 shutdown() 执行之前提交的任务都会被执行。

			- List<Runnable> shutdownNow();

				- 立即关闭，跳过所有正在执行的任务和被提交还没有执行的任务。但是它并不对正在执行的任务做任何保证，有可能它们都会停止，也有可能执行完成。
				- 返回等待执行的任务列表

			- boolean isShutdown();

				- 判断线程池是否已关闭

			- boolean isTerminated();

				- 如果调用了 shutdown() 或 shutdownNow() 方法后，所有任务结束了，那么该方法返回 true
				- 这个方法必须在调用 shutdown 或 shutdownNow方 法之后调用才会返回 true

			- boolean awaitTermination(long timeout, TimeUnit unit)

				- 等待所有任务完成，并设置超时时间
				- 先调用 shutdown 或 shutdownNow，然后再调这个方法等待所有的线程真正地完成，返回值意味着有没有超时

			- <T> Future<T> submit(Callable<T> task);

				- 返回一个 Future 对象，除此之外，submit(Callable) 接收的是一个 Callable 的实现，Callable 接口中的 call() 方法有一个返回值，可以返回任务的执行结果，而 Runnable 接口中的 run() 方法是 void 的，没有返回值。

			- <T> Future<T> submit(Runnable task, T result);

				- 提交一个 Runnable 任务，第二个参数将会放到 Future 中，作为返回值

			- Future<?> submit(Runnable task);

				- 可以返回一个 Future 对象，通过返回的 Future 对象，我们可以检查提交的任务是否执行完毕（future.get()，返回 null 说明任务正确执行完成，方法会阻塞）。

			- <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)

				- 接收一个 Callable 集合，执行之后会返回一个 Future 的 List，其中对应着每个 Callable 任务执行后的 Future 对象。

			-  <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                  long timeout, TimeUnit unit)

				- 执行所有任务，设置超时时间

			- <T> T invokeAny(Collection<? extends Callable<T>> tasks)

				- 接收的是一个 Callable 的集合，执行这个方法不会返回 Future，但是会返回所有 Callable 任务中其中一个任务的执行结果。这个方法也无法保证返回的是哪个任务的执行结果，反正是其中的某一个。

			- <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit)

				- 带超时，超过指定的时间，抛出 TimeoutException 异常

	- execute 声明了执行一个任务，Runnable 表示任务，作用就是执行提交的任务，仅此而已。但是该方式提交的任务没有返回值，所以无法判断任务是否执行成功。

- CompletionService，接口，在一个对象里发送任务给执行器，然后在另一个对象里处理结果。接口声明发送和处理两种方法实现适配这种情形

	- submit

		- 发送任务给执行器

	- poll

		- 无参数的 poll() 方法用于检查队列中是否有 Future 对象。如果队列为空，则立即返回 null。否则，它将返回队列中的第一个元素，并移除这个元素。

	- take

		- 这个方法也没有参数，它检查队列中是否有Future对象。如果队列为空，它将阻塞线程直到队列中有可用的元素。如果队列中有元素，它将返回队列中的第一个元素，并移除这个元素。

	- ExecutorCompletionService，实现类

		- CompletionService<String> service=new ExecutorCompletionService<>(executor);

使用 Executor 对象来执行任务

			- 优势：可以共享 CompletionService 对象，并发送任务到执行器，然后其他的对象可以处理任务的结果。
			- 不足：它只能为已经执行结束的任务获取 Future 对象，因此，这些 Future 对象只能被用来获取任务的结果。

- RejectedExecutionHandler，接口

  当队列和线程池都满了，说明线程池处于饱和状态，那么必须采取一种策略处理提交的新任务。

	- 声明 rejectedExecution(Runnable r, ThreadPoolExecutor executor) 方法，是一种饱和策略，当队列满了并且线程个数达到 maximunPoolSize 后采取的策略。

		- Runnable r：存储被拒绝的任务
		- ThreadPoolExecutor executor：存储拒绝任务的执行者

	- 策略实现

		- ThreadPoolExecutor.DiscardPolicy

			- 默默丢弃，不抛出异常，什么也不做

		- ThreadPoolExecutor.DiscardOldestPolicy

			- 丢弃队列里最近的一个任务，并执行当前任务。
			- 在线程池的等待队列中，将头取出一个抛弃，然后将当前线程放进去。
			- 调用 poll 丢弃一个任务，执行当前任务

		- ThreadPoolExecutor.CallerRunsPolicy

			- 只用调用者所在线程来运行任务。
			- 如果发现线程池还在运行，就直接运行这个线程
			- 在队列满时，让提交任务的线程去执行任务，相当于让生产者临时去干了消费者干的活儿，这样生产者虽然没有被阻塞，但提交任务也会被暂停。

		- ThreadPoolExecutor.AbortPolicy，默认策略，直接抛出一个 RejectedExecutionException
		- 自定义

			- 记录日志或持久化不能处理的任务

- TimeUnit，枚举，表示给定单元粒度的时间段。

	- 枚举属性

		- NANOSECONDS，纳秒
		- MICROSECONDS，微秒
		- MILLISECONDS，毫秒
		- SECONDS，秒
		- MINUTES，分钟
		- HOURS，小时
		- DAYS，天

	- timedWait(Object obj, long timeout)
	- timedJoin(Thread thread, long timeout)
	- sleep(long timeout)

### Collections

- Queue

	- ConcurrentLinkedQueue

		- 使用 CAS 非阻塞算法实现
		- 并发队列，提供了比 synchronized 机制更高的性能和可伸缩性。
		- 空队列通常都包含一个「哨兵节点」或者叫「哑节点」，并且头节点和尾节点在初始化时都指向该哨兵节点。
		- 尾节点通常要么指向哨兵节点（队列为空时），要么指向倒数第二个元素（有操作正在执行更新）

	- BlockingQueue（使用锁实现）

		- ArrayBlockingQueue

			- 一个由数组结构组成的有界阻塞队列，默认为非公平锁
			- 通过使用全局独占锁实现同时只能有一个线程进行入队或者出队操作，这个锁的粒度比较大，有点类似在方法上添加synchronized的意味。
			- 另外相比 LinkedBlockingQueue、ArrayBlockingQueue的size 操作的结果是精确的，因为计算前加了全局锁。

		- DelayQueue

			- 一个使用优先级队列实现的无界阻塞队列
			- DelayQueue 队列中每个元素都有个过期时间，并且队列是个优先级队列，当从队列获取元素时候，只有过期元素才会出队列。
			- 内部使用的是 PriorityQueue 存放数据，使用 ReentrantLock 实现线程同步，所以是阻塞队列。
			- 队列里面的元素要实现 Delayed 接口，一个是获取当前剩余时间的接口，一个是元素比较的接口，因为是有优先级的队列。

		- LinkedBlockingQueue

			- 一个由链表结构组成的无界阻塞队列，使用独占锁 ReentrantLock 实现，这里边无界的概念是只要有内存就能一直存储元素
			- 主要构成

				- 两个 Node 分别用来存放首尾节点
				- 初始值为 0 的原子变量 count 用来记录队列元素个数
				- 两个ReentrantLock 的独占锁，分别用来控制元素入队和出队加锁，其中 takeLock 用来控制同时只有一个线程可以从队列获取元素，其他线程必须等待，putLock 控制同时只能有一个线程可以获取锁去添加元素，其他线程必须等待。
				- 另外 notEmpty 和 notFull 用来实现入队和出队的同步。由于出入队是两个非公平独占锁，所以可以同时又一个线程入队和一个线程出队，其实这个是个生产者-消费者模型。

			- 主要方法

				- offer 操作-生产者，带超时间和不带
				- put 操作-生产者
				- poll 操作-消费者，带超时时间和不带
				- take 操作-消费者
				- size 操作
				- peek 操作
				- remove操作

		- PriorityBlockingQueue

			- 一个支持优先级排序的无界阻塞队列，每次出队都返回优先级最高的元素，是二叉树最小堆的实现，直接遍历队列元素是无序的。

		- SynchronousQueue

			- 一个不存储元素的阻塞队列
			- 生产者线程对其的插入操作 put 必须等待消费者的移除操作 take，反过来也一样。
			- 生产者和消费者互相等待对方，握手，然后一起离开。

		- TransferQueue

			- LinkedTransferQueue

				- 一个由链表结构组成的无界阻塞队列
				- 生产者会一直阻塞直到所添加到队列的元素被某一个消费者所消费（不仅仅是添加到队列里就完事）
				- 在队列中已有元素的情况下，调用 transfer 方法，可以确保队列中被传递元素之前的所有元素都能被处理。

	- Deque

		- ArrayDeque
		- LinkedList

			- 双端队列、线程不安全
			- 双链表实现了 List 和 Deque 接口。
			- 实现所有可选列表操作，并允许所有元素（包括 null ）。
			- 链表批量增加，是靠 for 循环遍历原数组，依次执行插入节点操作。对比 ArrayList 是通过 System.arraycopy 完成批量增加的。

		- BlockingQueue

			- LinkedBlockingDeque

				- 一个由链表结构组成的双向阻塞队列

- CopyOnWriteArrayList

	- Copy-On-Write 简称 COW，是一种用于程序设计中的优化策略。其基本思路是，从一开始大家都在共享同一个内容，当某个人想要修改这个内容的时候，才会真正把内容 Copy 出去形成一个新的内容然后再改，这是一种延时懒惰策略。
	- 注意事项

		- 减少扩容开销，尽量合理设置初始容量大小
		- 尽量使用批量添加，因为每次添加都需要复制，因此尽量避免减少添加次数

	- 缺点

		- 内存占用，毕竟要进行复制，还要就得容器提供使用，容易引起频繁的 Young GC 或者是 Full GC
		- 数据一致性问题

			- 只能保证最终的一致性而不能保证数据的实时一致性

- CopyOnWriteArraySet
- ConcurrentSkipListSet

	- 通过 ConcurrentNavigableMap 来实现的，它是一个有序的线程安全的集合。

- ConcurrentMap

	- ConcurrentHashMap

		- 在 1.7 及以下，ConcurrentHashMap 使用的是数组加链表，Segment + ReentrantLock，锁分段
		- 在 1.8 后，对 ConcurrentHashMap 做了一些调整

			- 链表长度 >= 8时，链表会转换为红黑树，<= 6 时又会恢复成链表；
			- 1.7 及以前，链表采用的是头插法，1.8 后改成了尾插法；
			- Segment + ReentrantLock 改成了 CAS + synchronized。取消 Segment，直接利用 table 数组单元作为锁，实现了可对每行数据加锁，进一步提高了并发性能。
			- 链表的长度超过了 8，那么链表将转换为红黑树。（桶的数量必须大于 64，小于 64 的时候只会扩容）

		- 1.7 查询遍历链表效率太低，因此 1.8 做了一些数据结构上的调整，也将 1.7 中存放数据的 HashEntry 改为 Node，但作用都是相同的。

	- ConcurrentNavigableMap

		- ConcurrentSkipListMap

	- HashMap &  ConcurrentHashMap

		- HashMap（jdk1.7）

			- 核心成员变量

				- 初始桶大小----16，可自定义
				- 桶最大值----2^30
				- 负载因子----0.75，可自定义
				- table----也就是俗话说的桶，整整存放数据的数组
				- size----Map 存放元素的数量

			- 方法

				- put 方法

					- 判断当前数组是否需要初始化。
					- 如果 key 为空，则 put 一个空值进去。
					- 根据 key 计算出 hashcode。
					- 根据计算出的 hashcode 定位出所在桶。
					- 如果桶是一个链表则需要遍历判断里面的 hashcode、key 是否和传入 key 相等，如果相等则进行覆盖，并返回原来的值。
					- 如果桶是空的，说明当前位置没有数据存入；新增一个 Entry 对象写入当前位置。
					- 当调用 addEntry 写入 Entry 时需要判断是否需要扩容。如果需要就进行两倍扩充，并将当前的 key 重新 hash 并定位。

				- get 方法

					- 首先也是根据 key 计算出 hashcode，然后定位到具体的桶中。
					- 判断该位置是否为链表。
					- 不是链表就根据 key、key 的 hashcode 是否相等来返回值。
					- 为链表则需要遍历直到 key 及 hashcode 相等时候就返回值。
					- 啥都没取到就直接返回 null 。

		- HashMap（jdk1.8）

			-  Hash 冲突严重时，在桶上形成的链表会变的越来越长，这样在查询时的效率就会越来越低；时间复杂度为 O(N)。
			- 核心改进项

				- TREEIFY_THRESHOLD 用于判断是否需要将链表转换为红黑树的阈值。
				- HashEntry 修改为 Node。

			- 方法

				- put 方法

					- 判断当前桶是否为空，空的就需要初始化（resize 中会判断是否进行初始化）。
					- 根据当前 key 的 hashcode 定位到具体的桶中并判断是否为空，为空表明没有 Hash 冲突就直接在当前位置创建一个新桶即可。
					- 如果当前桶有值（ Hash 冲突），那么就要比较当前桶中的 key、key 的 hashcode 与写入的 key 是否相等
					- 如果当前桶为红黑树，那就要按照红黑树的方式写入数据。
					- 如果是个链表，就需要将当前的 key、value 封装成一个新节点写入到当前桶的后面（形成链表）。
					- 接着判断当前链表的大小是否大于预设的阈值，大于时就要转换为红黑树。
					- 如果在遍历过程中找到 key 相同时直接退出遍历。存在相同的 key，那就需要将值覆盖。
					- 最后判断是否需要进行扩容。

				- get 方法

					- 首先将 key hash 之后取得所定位的桶。
					- 如果桶为空则直接返回 null 。
					- 否则判断桶的第一个位置(有可能是链表、红黑树)的 key 是否为查询的 key，是就直接返回 value。
					- 如果第一个不匹配，则判断它的下一个是红黑树还是链表。
					- 红黑树就按照树的查找方式返回值。
					- 不然就按照链表的方式遍历匹配返回值。

		- HashMap 的缺陷（或者说为什么不是线程安全的）

			- 并发场景下使用时容易出现死循环
			- 多个线程进行扩容只会有一个成功，数据丢失

		- ConcurrentHashMap（1.7）

			- Segment 数组、HashEntry 组成，和 HashMap 一样，仍然是数组加链表。
			- 采用了分段锁技术，其中 Segment 继承于 ReentrantLock。不会像 HashTable 那样不管是 put 还是 get 操作都需要做同步处理，理论上 ConcurrentHashMap 支持 CurrencyLevel (Segment 数组数量)的线程并发。每当一个线程占用锁访问一个 Segment 时，不会影响到其他的 Segment。
			- 方法

				- put 方法

					- 将当前 Segment 中的 table 通过 key 的 hashcode 定位到 HashEntry。
					- 遍历该 HashEntry，如果不为空则判断传入的 key 和当前遍历的 key 是否相等，相等则覆盖旧的 value。
					- 不为空则需要新建一个 HashEntry 并加入到 Segment 中，同时会先判断是否需要扩容。
					- 最后会解除所获取当前 Segment 的锁。

				- get 方法

					- 只需要将 Key 通过 Hash 之后定位到具体的 Segment ，再通过一次 Hash 定位到具体的元素上。

		- ConcurrentHashMap（1.8）

			- 解决查询遍历链表效率太低，抛弃了原有的 Segment 分段锁，而采用了 CAS + synchronized 来保证并发安全性。
			- 方法

				- put 方法

					- 根据 key 计算出 hashcode 。
					- 判断是否需要进行初始化。
					- 利用 CAS 尝试写入，失败则自旋保证成功。
					- 如果当前位置的 hashcode == MOVED == -1,则需要进行扩容。
					- 如果都不满足，则利用 synchronized 锁写入数据。
					- 如果数量大于 TREEIFY_THRESHOLD 则要转换为红黑树。

				- get 方法

					- 根据计算出来的 hashcode 寻址，如果就在桶上那么直接返回值。
					- 如果是红黑树那就按照树的方式获取值。
					- 不满足那就按照链表的方式遍历获取值。

## locks

### AbstractOwnableSynchronizer，主要提供一个exclusiveOwnerThread 属性，用于关联当前持有该锁的线程。

- AbstractQueuedSynchronizer，一个同步器，获取锁和释放锁，分独占和共享两种模式，模板设计模式，抽象类

  tryAcquire、tryRelease不定义成抽象的原因：独占和共享没必要都去实现一遍。

	- CLH 锁，一种基于双向链表（隐式创建）的高性能、公平的自旋锁，申请加锁的线程只需要在其前驱节点的本地变量上自旋，从而极大地减少了不必要的处理器缓存同步的次数，降低了总线和内存的开销。

		- CLH 锁的节点对象只有一个 active 属性
		- CLH 锁的节点属性 active 的改变是由其自身触发的
		- CLH 锁是在前驱节点的 active 属性上进行自旋

	- 主要工作思路

		- 在获取锁时候，先判断当前状态是否允许获取锁，若是可以则获取锁，否则获取不成功。获取不成功则会阻塞，进入阻塞队列。而释放锁时，一般会修改状态位，唤醒队列中的阻塞线程。

	- CAS + state 状态值

		- 比较并交换，是并发控制操作的基础。CAS 有三个值：内存值、原始值、修改值，如果原始值不等于内存值，返回 false；如果等于则修改，返回 true，并将内存值修改为修改值。

	- 实现

		- 共享

			- CountDownLatch（门阀）
			- Semaphor（信号量）
			- ReadWriteLock（读写锁的读锁）

		- 排他

			- ReentrantLock
			- ReadWriteLock（读写锁的写锁）

	- Node

		- int waitStatus

			- CANCELLED = 1，表示当前的线程被取消
			- waitStatus = 0，表示当前节点在 sync 队列中，等待着获取锁
			- SIGNAL = -1，表示当前节点的后继节点包含的线程需要运行
			- CONDITION = -2，表示当前节点在等待 condition，也就是在 condition 队列中
			- PROPAGATE = -3，表示当前场景下后续的 acquireShared 能够得以执行

		- Node prev、Node next、Node nextWaiter

	- state

		- 有多少个线程取得了锁，对于互斥锁来说 state <= 1
		- state = 0，锁不被任何线程所占有

	- acquire

		- 功能

			- 状态的维护

				- 需要在锁定时，需要维护一个状态（int 类型），而对状态的操作是原子和非阻塞的，通过同步器提供的对状态访问的方法对状态进行操纵，并且利用 compareAndSet 来确保原子性的修改。

			- 状态的获取

				- 一旦成功的修改了状态，当前线程或者说节点，就被设置为头节点。

			- sync 队列的维护

				- 在获取资源未果的过程中条件不符合的情况下（不该自己，前驱节点不是头节点或者没有获取到资源）进入睡眠状态，停止线程调度器对当前节点线程的调度。

		- tryAcquire
acquire
tryAcquireShared
doAcquireShared
acquireQueued
tryAcquireNanos
doAcquireNanos
tryAcquireSharedNanos
doAcquireSharedNanos
acquireSharedInterruptibly
doAcquireSharedInterruptibly
acquireInterruptibly
doAcquireInterruptibly
shouldParkAfterFailedAcquire
cancelAcquire

	- release

		- tryRelease
release
releaseShared
tryReleaseShared
doReleaseShared
fullyRelease

- AbstractQueuedLongSynchronizer，64位版本的同步器

### Condition

- 多线程间协调通信的工具类，使得某个或者某些线程一起等待某个条件（Condition）,只有当该条件具备( signal 或者 signalAll 方法被调用)时 ，这些等待线程才会被唤醒，从而重新争夺锁。
- 两个 node 分别用来存放条件队列的首尾节点，条件队列就是调用条件变量的 await 方法被阻塞后的节点组成的单向链表。
- ConditionObject 还要依赖 AQS 的 state，ConditionObject 是 AQS 类的一个内部类。

### Lock（接口）

- 实现

	- ReentrantLock

		- 公平锁
		- 非公平锁

	- ReentrantReadWriteLock.ReadLock
	- ReentrantReadWriteLock.WriteLock

- 核心方法声明

	- tryLock()

		- 仅在调用时锁为空闲状态才获取该锁。如果锁可用，则获取锁，并立即返回值  true 。如果锁不可用，则此方法将立即返回值  false 。通常对于那些不是必须获取锁的操作可能有用。

	- lock()

		- 获取锁。如果锁不可用，出于线程调度目的，将禁用当前线程，并且在获得锁之前，该线程将一直处于休眠状态。

	- unlock()

		- 释放锁。对应于 lock()、tryLock()、tryLock(xx)、lockInterruptibly() 等操作，如果成功的话应该对应着一个 unlock()，这样可以避免死锁或者资源浪费。

	- lockInterruptibly()

		- 如果当前线程未被中断，则获取锁。
		- 如果锁可用，则获取锁，并立即返回。
		- 如果锁不可用，出于线程调度目的，将禁用当前线程，并且在发生以下两种情况之一以前，该线程将一直处于休眠状态：

			- 锁由当前线程获得；

				- 如果当前线程：在进入此方法时已经设置了该线程的中断状态；

			- 或者其他某个线程中断 当前线程，并且支持对锁获取的中断。

				- 或者在获取锁时被中断 ，并且支持对锁获取的中断，则将抛出  InterruptedException ，并清除当前线程的已中断状态。

	- tryLock(long time, TimeUnit unit)

		- 如果锁在给定的等待时间内空闲，并且当前线程未被中断，则获取锁。如果锁可用，则此方法将立即返回值  true 。如果锁不可用，出于线程调度目的，将禁用当前线程

	- newCondition() 

		- 返回用来与此 Lock 实例一起使用的 Condition 实例。

### LockSupport

- 线程的阻塞与唤醒

	- park()
	- unpark()

### ReadWriteLock（接口）

- 声明读锁和写锁

	- Lock readLock();
	- Lock writeLock();

- ReentrantReadWriteLock

	- 读写锁维护了一对相关的锁，一个用于只读操作，一个用于写入操作。只要没有写，读取锁可以由多个读线程同时保持。写入锁是独占的。
	- 当队列中的头节点为读锁时，代表读操作可以执行，而写操作不能执行，因此请求写操作的线程会被挂起，当读操作依次退出后，写锁成为头节点，请求写操作的线程被唤醒，可以执行写操作，而此时的读请求将被封装成 Node 放入 AQS 的队列中。如此往复，实现读写锁的读写交替进行。
	-  适用于读多写少的情况。
	-  AQS 的状态是32位（int 类型）的，分成两份，读锁用高 16 位，表示持有读锁的线程数（sharedCount），写锁低16位，表示写锁的重入次数 （exclusiveCount）。
	- 状态值为 0 表示锁空闲，sharedCount不为 0 表示分配了读锁，exclusiveCount 不为 0 表示分配了写锁，sharedCount 和 exclusiveCount 肯定不会同时不为 0。

- StampedLock.ReadWriteLockView

### StampedLock

- jdk8 版本新增的一个锁，该锁提供了三种模式的读写控制

	- 写锁 writeLock

		- 是个排它锁或者叫独占锁，同时只有一个线程可以获取该锁，当一个线程获取该锁后，其它请求的线程必须等待，当目前没有线程持有读锁或者写锁的时候才可以获取到该锁，请求该锁成功后会返回一个 stamp 票据变量用来表示该锁的版本，当释放该锁时候需要 unlockWrite 并传递参数 stamp。

	- 悲观读锁 readLock

		- 是个共享锁，在没有线程获取独占写锁的情况下，同时多个线程可以获取该锁，如果已经有线程持有写锁，其他线程请求获取该读锁会被阻塞。这里讲的悲观其实是参考数据库中的乐观悲观锁的，这里说的悲观是说在具体操作数据前悲观的认为其他线程可能要对自己操作的数据进行修改，所以需要先对数据加锁，这是在读少写多的情况下的一种考虑,请求该锁成功后会返回一个 stamp 票据变量用来表示该锁的版本，当释放该锁时候需要 unlockRead 并传递参数 stamp。

	- 乐观读锁 tryOptimisticRead

		- 是相对于悲观锁来说的，在操作数据前并没有通过 CAS 设置锁的状态，如果当前没有线程持有写锁，则简单的返回一个非 0 的 stamp 版本信息，获取该 stamp 后在具体操作数据前还需要调用 validate 验证下该 stamp 是否已经不可用，也就是看当调用 tryOptimisticRead 返回 stamp 后到到当前时间间是否有其他线程持有了写锁，如果是那么 validate 会返回 0，否则就可以使用该 stamp 版本的锁对数据进行操作。由于 tryOptimisticRead 并没有使用 CAS 设置锁状态所以不需要显示的释放该锁。 该锁的一个特点是适用于读多写少的场景，因为获取读锁只是使用与或操作进行检验，不涉及 CAS 操作，所以效率会高很多，但是同时由于没有使用真正的锁，在保证数据一致性上需要拷贝一份要操作的变量到方法栈，并且在操作数据时候可能其他写线程已经修改了数据，而我们操作的是方法栈里面的数据，也就是一个快照，所以最多返回的不是最新的数据，但是一致性还是得到保障的。

## atomic（原子并发类包），主要是对 CAS 原理的掌握

### AtomicBoolean

- 可以用原子方式更新的 boolean 值。

### AtomicInteger

- 可以用原子方式更新的 int 值。

### AtomicIntegerArray

- 可以用原子方式更新其元素的 int 数组。

### AtomicIntegerFieldUpdater

- 基于反射的实用工具，可以对指定类的指定 volatile int 字段进行原子更新。

### AtomicLong

- 可以用原子方式更新的 long 值。

### AtomicLongArray

- 可以用原子方式更新其元素的 long 数组。

### AtomicLongFieldUpdater

- 基于反射的实用工具，可以对指定类的指定 volatile long 字段进行原子更新。

### AtomicMarkableReference

- 维护带有标记位的对象引用，可以原子方式对其进行更新。

### AtomicReference

- 可以用原子方式更新的对象引用。

### AtomicReferenceArray

- 可以用原子方式更新其元素的对象引用数组。

### AtomicReferenceFieldUpdater

- 基于反射的实用工具，可以对指定类的指定 volatile 字段进行原子更新。

### AtomicStampedReference

- 维护带有整数「标志」的对象引用，可以用原子方式对其进行更新。

### Striped64

把逻辑上连续的数据分为多个段，使这一序列的段存储在不同的物理设备上。通过把段分散到多个设备上可以增加访问并发性，从而提升总体的吞吐量。

通过一个 Cell 数组维持了一序列分解数的表示，通过 base 字段维持数的初始值，通过 cellsBusy 字段来控制 resizing 和/或创建 Cell。它还提供了对数进行累加的机制。

- 这个类维护一个延迟初始的、原子地更新值的表，加上额外的「base」 字段。表的大小是 2 的幂。当表的条目上出现竞争时，在到达容量前表扩容一倍，通过增加条目来减少竞争。
- DoubleAccumulator
- DoubleAdder

	- 底层通过转换为 Long 来进行运算

- LongAccumulator

	- 相比于 LongAdder，支持各种自定义运算的 long 运算

- LongAdder

	- JDK8 新增，高并发环境下比 AtomicLong 更高效

## 关键字总结

### volatile

- 变量可见性问题

  在一个多线程的应用中，线程在操作非 volatile 变量时，出于性能考虑，每个线程可能会将变量从主存拷贝到 CPU 缓存中。如果你的计算机有多个 CPU，每个线程可能会在不同的 CPU 中运行。这意味着，每个线程都有可能会把变量拷贝到各自 CPU 的缓存中。
  
  对于非 volatile 变量，JVM 并不保证会从主存中读取数据到 CPU 缓存，或者将 CPU 缓存中的数据写到主存中。

	- volatile 关键字就是设计用来解决变量可见性问题。将变量声明为 volatile，则在写入变量时，也会同时将变量值写入到主存中。同样的，在读取变量值时，也会直接从主存中读取。

- 完整的 volatile 可见性保证

	- 如果线程 A 写入一个 volatile 变量，线程 B 随后读取了同样的 volatile 变量，则线程 A 在写入 volatile 变量之前的所有可见的变量值，在线程 B 读取 volatile 变量后也同样是可见的。
	- 如果线程A读取一个volatile变量，那么线程A中所有可见的变量也会同样从主存重新读取。

- 指令重排序问题

	- volatile 可以保证 Happens-Before 原则

- volatile 不能保证原子性

	- 多个线程都能写入共享的 volatile 变量，主存中也能存储正确的变量值，然而这有一个前提，变量新值的写入不能依赖于变量的旧值。换句话说，就是一个线程写入一个共享 volatile 变量值时，不需要先读取变量值，然后以此来计算出新的值。
	- 如果线程需要先读取一个 volatile 变量的值，以此来计算出一个新的值，那么 volatile 变量就不足够保证正确的可见性。

### synchronized

- CAS

	- Compare and Swap，比较并设置。用于在硬件层面上提供原子性操作。在 Intel 处理器中，比较并交换通过指令 cmpxchg实现。比较是否和给定的数值一致，如果一致则修改，不一致则不修改。

- Java中的每一个对象都可以作为锁

	- 对于同步方法，锁是当前实例对象。
	- 对于静态同步方法，锁是当前对象的Class对象。
	- 对于同步方法块，锁是 synchonized 括号里配置的对象。

- synchronized 同步的原理

	- JVM 基于进入和退出 Monitor 对象来实现方法同步和代码块同步，使用 monitorenter 和 monitorexit 指令实现，monitorenter 指令是在编译后插入到同步代码块的开始位置，而 monitorexit 是插入到方法结束处和异常处， JVM 要保证每个 monitorenter 必须有对应的 monitorexit 与之配对
	- 任何对象都有一个 monitor 与之关联，当且一个 monitor 被持有后，它将处于锁定状态。线程执行到 monitorenter 指令时，将会尝试获取对象所对应的 monitor 的所有权，即尝试获得对象的锁。

- Java 对象头

	- Mark Word（存储对象的 hashCode 或锁信息等）

		- 锁状态（默认无锁）
		- 对象的 hashCode（25位）
		- 对象分代年龄（4位）
		- 是否是偏向锁（1位）
		- 锁标志位（2位）

			- 在运行期间 Mark Word 里存储的数据会随着锁标志位的变化而变化

				- https://pic.downk.cc/item/5eec6f1314195aa594e6dc17.png

	- Class Metadata Address

		- 存储到对象类型数据的指针

	- Array length

		- 数组的长度（如果当前对象是数组）

- 锁的升级，只能升级不能降级

	- 无锁
	- 偏向锁

		- 大多数情况下锁不仅不存在多线程竞争，而且总是由同一线程多次获得，为了让线程获得锁的代价更低而引入了偏向锁
		- 优点：加锁和解锁不需要额外的消耗，和执行非同步方法比仅存在纳秒级的差距。
缺点：如果线程间存在锁竞争，会带来额外的锁撤销的消耗。
适用场景：适用于只有一个线程访问同步块场景。

	- 轻量级锁

		- 线程在执行同步块之前，JVM 会先在当前线程的栈桢中创建用于存储锁记录的空间，并将对象头中的 Mark Word 复制到锁记录中。然后线程尝试使用 CAS 将对象头中的 Mark Word 替换为指向锁记录的指针。如果成功，当前线程获得锁，如果失败，表示其他线程竞争锁，当前线程便尝试使用自旋来获取锁。
		- 优点：竞争的线程不会阻塞，提高了程序的响应速度。
缺点：如果始终得不到锁竞争的线程使用自旋会消耗 CPU。
适用场景：追求响应时间；同步块执行速度非常快；

	- 重量级锁

		- 优点：线程竞争不使用自旋，不会消耗CPU。
缺点：线程阻塞，响应时间缓慢。
适用场景：追求吞吐量；同步块执行速度较长；

