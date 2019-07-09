# Java 并发编程（java.util.concurrent）

## atomic

## locks

### AbstractOwnableSynchronizer，主要提供一个exclusiveOwnerThread属性，用于关联当前持有该锁的线程。

- AbstractQueuedSynchronizer，一个同步器，获取锁和释放锁，分独占和共享两种模式，模板设计模式，抽象类

  tryAcquire、tryRelease不定义成抽象的原因：独占和共享没必要都去实现一遍。

	- CLH锁，一种基于单向链表(隐式创建)的高性能、公平的自旋锁，申请加锁的线程只需要在其前驱节点的本地变量上自旋，从而极大地减少了不必要的处理器缓存同步的次数，降低了总线和内存的开销。

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
			- ReadWriteLock（读写锁）

		- 排他

			- ReentrantLock

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
	- lockInterruptibly()
	- tryLock(long time, TimeUnit unit)

### LockSupport

### ReadWriteLock

### ReentrantReadWriteLock

### StampedLock

## concurrent

### Executor 框架，线程池

- 子主题 1
- 子主题 2
- Executor，接口，只有一个「void execute(Runnable command);」方法 

	- ExecutorService，接口，定义完整的线程池行为

		- AbstractExecutorService，抽象类

			- ThreadPoolExecutor
			- ForkJoinPool
			- DelegatedExecutorService

		- ScheduledExecutorService，接口

			- Executors 的静态内部类：ScheduledThreadPoolExecutor
			- Executors 的静态内部类：DelegatedScheduledExecutorService

		- 声明的方法

			- void shutdown();

				- 不会立即关闭，但是它不再接收新的任务，直到当前所有线程执行完成才会关闭，所有在 shutdown() 执行之前提交的任务都会被执行。

			- List<Runnable> shutdownNow();

				- 立即关闭，跳过所有正在执行的任务和被提交还没有执行的任务。但是它并不对正在执行的任务做任何保证，有可能它们都会停止，也有可能执行完成。

			- boolean isShutdown();
			- boolean isTerminated();
			- boolean awaitTermination(long timeout, TimeUnit unit)
			- <T> Future<T> submit(Callable<T> task);

				- 返回一个 Future 对象，除此之外，submit(Callable) 接收的是一个 Callable 的实现，Callable 接口中的 call() 方法有一个返回值，可以返回任务的执行结果，而 Runnable 接口中的 run() 方法是 void 的，没有返回值。

			- <T> Future<T> submit(Runnable task, T result);
			- Future<?> submit(Runnable task);

				- 可以返回一个 Future 对象，通过返回的 Future 对象，我们可以检查提交的任务是否执行完毕（future.get()，返回 null 说明任务正确执行完成，方法会阻塞）。

			- <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)

				- 接收一个 Callable 集合，执行之后会返回一个 Future 的 List，其中对应着每个 Callable 任务执行后的 Future 对象。

			-  <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                  long timeout, TimeUnit unit)
			- <T> T invokeAny(Collection<? extends Callable<T>> tasks)

				- 接收的是一个 Callable 的集合，执行这个方法不会返回 Future，但是会返回所有 Callable 任务中其中一个任务的执行结果。这个方法也无法保证返回的是哪个任务的执行结果，反正是其中的某一个。

			- <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit)

	- execute 声明了执行一个任务，Runnable 表示任务，作用就是执行提交的任务，仅此而已

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

### Tools

*XMind: ZEN - Trial Version*