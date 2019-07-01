package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import sun.misc.Unsafe;

public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer implements java.io.Serializable {

    private static final long serialVersionUID = 7373984972572414691L;

    protected AbstractQueuedSynchronizer() { }

    static final class Node {
        static final Node SHARED = new Node();
        static final Node EXCLUSIVE = null;
		
		// 节点的等待状态
		// 值为 0,表示当前节点在 sync 队列中，等待着获取锁
        volatile int waitStatus;
		// 节点操作因为超时或者对应的线程被 interrupt。
		// 节点不应该留在此状态，一旦达到此状态将从 CHL 队列中踢出。
		// 表示当前的线程被取消
		static final int CANCELLED =  1; 
		// 节点的后继节点是（或者将要成为）BLOCKED 状态（例如通过 LockSupport.park() 操作），因此一个节点一旦被释放（解锁）或者取消就需要唤醒（LockSupport.unpack()）它的后继节点。
		// 表示当前节点的后继节点包含的线程需要运行
		static final int SIGNAL    = -1;
		// 节点对应的线程因为不满足一个条件（Condition）而被阻塞。
		// 表示当前节点在等待condition，也就是在condition队列中
		static final int CONDITION = -2;
		// 表示当前场景下后续的 acquireShared 能够得以执行
		static final int PROPAGATE = -3;
		
		// 前驱节点，当前节点被取消，那就需要前驱节点和后继节点来完成连接
        volatile Node prev;
		
		// 后继节点
        volatile Node next;
        
		// 存储 condition 队列中的后继节点
        Node nextWaiter;
		
		// 入队列时的当前线程
		volatile Thread thread;
		
        final boolean isShared() {
            return nextWaiter == SHARED;
        }
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {
        }

        Node(Thread thread, Node mode) {
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) {
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

	// 头结点
    private transient volatile Node head;

	// 尾节点
    private transient volatile Node tail;
	
	// 有多少个线程取得了锁，对于互斥锁来说 state <= 1
    private volatile int state;

    protected final int getState() {
        return state;
    }

    protected final void setState(int newState) {
        state = newState;
    }

    protected final boolean compareAndSetState(int expect, int update) {
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    static final long spinForTimeoutThreshold = 1000L;
	
	/**
     * 确保进入的 Node 都会有机会顺序的添加到 sync 队列中
	 * 1.如果尾节点为空，那么原子化的分配一个头节点，并将尾节点指向头节点，这一步是初始化
	 * 2.然后是重复在 addWaiter 中做的工作，但是在一个 while(true) 的循环中，直到当前节点入队为止
     */
    private Node enq(final Node node) {
		// CAS"自旋"，直到成功加入队尾
        for (;;) {
            Node t = tail;
            if (t == null) {
				// 队列为空，创建一个空的标志结点作为head结点，并将tail也指向它。
				// 最开始 head 和 tail 都是空的，需要通过 CAS 做初始化，如果 CAS 失败，则循环重新检查 tail。
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
				//正常流程，放入队尾
				// head 和 tail 不是空的，说明已经完成初始化，和 addWaiter 方法的上半段一样，CAS 修改。
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

	/**
	 * 将当前线程加入到等待队列的队尾，并返回当前线程所在的结点。
     * 使用 LockSupport 将当前线程 unpark
	 * 1.使用当前线程构造Node
	 * 2.先行尝试在队尾添加
     */
    private Node addWaiter(Node mode) {
		// 以给定模式构造结点。mode 有两种：EXCLUSIVE（独占）和SHARED（共享）
        Node node = new Node(Thread.currentThread(), mode);
		// 快速尝试在尾部添加
        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
			// CAS 把 tail 设置成当前节点，如果成功的话就说明插入成功，直接返回 node，失败说明有其他线程也在尝试插入而且其他线程成功，如果是这样就继续执行 enq 方法。
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
		// 上一步失败则通过 enq 入队。
        enq(node);
        return node;
    }

    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }

	/**
     * 通过 LockSupport 的 unpark 方法将休眠中的线程唤醒，让其继续 acquire 状态
	 * node 一般为当前线程所在的结点
     */
    private void unparkSuccessor(Node node) {
		// 将状态设置为同步状态
        int ws = node.waitStatus;
		// 置零当前线程所在的结点状态，允许失败。
        if (ws < 0)
			// 获取当前节点的后继节点，如果满足状态，那么进行唤醒操作
			// 如果状态小于 0，把状态改成 0，0 是空的状态，因为 node 这个节点的线程释放了锁后续不需要做任何操作，不需要这个标志位，即便 CAS 修改失败了也没关系，其实这里如果只是对于锁来说根本不需要 CAS，因为这个方法只会被释放锁的线程访问，只不过 unparkSuccessor 这个方法是 AQS 里的方法就必须考虑到多个线程同时访问的情况（可能共享锁或者信号量这种）
            compareAndSetWaitStatus(node, ws, 0);
		// 如果没有满足状态，从尾部开始找寻符合要求的节点并将其唤醒
		// 找到下一个需要唤醒的结点 s
        Node s = node.next;
		//如果为空或已取消
		// 如果下一个节点为空或者下一个节点的状态 >0（目前大于 0 就是取消状态），则从 tail 节点开始遍历找到离当前节点最近的且 waitStatus <= 0（即非取消状态）的节点并唤醒
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
				// <= 0 的结点，都是还有效的结点。
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
			// 唤醒
            LockSupport.unpark(s.thread);
    }

    private void doReleaseShared() {
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if (h == head)                   // loop if head changed
                break;
        }
    }

	/**
	 * 主要用于检查状态，看看自己是否真的可以去休息了
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head;
		// head 指向自己
        setHead(node);
		// 如果还有剩余量，继续唤醒下一个邻居线程
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }
	
    private void cancelAcquire(Node node) {
        if (node == null)
            return;

        node.thread = null;

        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;
        Node predNext = pred.next;

        node.waitStatus = Node.CANCELLED;

        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                unparkSuccessor(node);
            }

            node.next = node;
        }
    }
	
	/**
	 * 主要用于检查状态，看看自己是否真的可以去休息了
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
		// 拿到前驱的状态
		// 获取 pred 前置节点的等待状态
        int ws = pred.waitStatus;
		// 前置节点状态是 signal，那当前节点可以安全阻塞，因为前置节点承诺执行完之后会通知唤醒当前节点
        if (ws == Node.SIGNAL)
			// 如果已经告诉前驱拿完号后通知自己一下，那就可以安心休息了
            return true;
        if (ws > 0) {
			// 如果前驱放弃了，那就一直往前找，直到找到最近一个正常等待的状态，并排在它的后边。
			// 那些放弃的结点会被 GC 回收
			// 前置节点如果已经被取消了，则一直往前遍历直到前置节点不是取消状态，与此同时会修改链表关系
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
			// 如果前驱正常，那就把前驱的状态设置成 SIGNAL，告诉它拿完号后通知自己一下。
			// SIGNAL 表示当前节点的后继节点包含的线程需要运行
			// 前置节点是 0 或者 propagate 状态，这里通过 CAS 把前置节点状态改成 signal
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
		// 这里不返回 true 让当前节点阻塞，而是返回 false，目的是让调用者再 check 一下当前线程是否能成功获取锁，失败的话再阻塞
        return false;
    }

    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

	
	/**
	 * 主要用于检查状态，看看自己是否真的可以去休息了
	 * 让线程去休息，真正进入等待状态。
     */
    private final boolean parkAndCheckInterrupt() {
		// 阻塞当前线程，当前 sync 对象
        LockSupport.park(this);
		// 阻塞返回后，返回当前线程是否被中断
        return Thread.interrupted();
    }

	/**
	 * 在等待队列中排队拿号（中间没其它事干可以休息），直到拿到号后再返回
     * 1.获取当前节点的前驱节点
	 * 2.当前驱节点是头结点并且能够获取状态，代表该当前节点占有锁
	 * 3.否则进入等待状态
     */
    final boolean acquireQueued(final Node node, int arg) {
		// 标记是否成功拿到资源
        boolean failed = true;
        try {
			// 标记等待过程中是否被中断过
            boolean interrupted = false;
			// 自旋
            for (;;) {
				// 拿到前驱
                final Node p = node.predecessor();
				//如果前驱是 head，即该结点已成老二，那么便有资格去尝试获取资源（可能是老大释放完资源唤醒自己的，当然也可能被 interrupt 了）。
				// 如果前置节点是 head，说明当前节点是队列第一个等待的节点，这时去尝试获取锁，如果成功了则获取锁成功。
                if (p == head && tryAcquire(arg)) {
					// 拿到资源后，将 head 指向该结点。所以 head 所指的标杆结点，就是当前获取到资源的那个结点或 null。
					// 队首且获取锁成功，把当前节点设置成 head，下一个节点成了等待队列的队首
                    setHead(node);
					// setHead 中 node.prev 已置为 null，此处再将 head.next 置为 null，就是为了方便 GC 回收以前的 head 结点。也就意味着之前拿完资源的结点出队了！
                    p.next = null; // help GC
                    failed = false;
					// 返回等待过程中是否被中断过
                    return interrupted;
                }
				// 如果自己可以休息了，就进入 waiting 状态，直到被 unpark()
				// 如果获取锁失败是否需要阻塞，如果需要的话就执行 parkAndCheckInterrupt 方法，如果不需要就继续循环。
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
					// 如果等待过程中被中断过，哪怕只有那么一次，就将 interrupted 标记为 true
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    private void doAcquireInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

	/**
     * 提供了具备有超时功能的获取状态的调用，如果在指定的 nanosTimeout 内没有获取到状态，那么返回false，反之返回true。
	 * 可以将该方法看做 acquireInterruptibly 的升级版，也就是在判断是否被中断的基础上增加了超时控制。
	 * 1.加入sync队列
	 * 2.条件满足直接返回
	 * 3.获取状态失败休眠一段时间
	 * 4.计算再次休眠的时间
	 * 5.休眠时间的判定
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null;
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

	/**
     * 以共享模式获取状态，共享模式和独占模式有所区别。
	 * 以文件的查看为例，如果一个程序在对其进行读取操作，那么这一时刻，对这个文件的写操作就被阻塞，相反，这一时刻另一个程序对其进行同样的读操作是可以进行的。
	 * 如果一个程序在对其进行写操作，那么所有的读与写操作在这一时刻就被阻塞，直到这个程序完成写操作。
	 * 1.尝试获取共享状态
	 * 2.获取失败进入sync队列
	 * 3.循环内判断退出队列条件
	 * 4.获取共享状态成功
	 * 5.获取共享状态失败
     */
    private void doAcquireShared(int arg) {
		// 加入队列尾部
        final Node node = addWaiter(Node.SHARED);
		// 是否成功标志
        boolean failed = true;
        try {
			// 等待过程中是否被中断过的标志
            boolean interrupted = false;
            for (;;) {
				// 前驱
                final Node p = node.predecessor();
				// 如果到 head 的下一个，因为 head 是拿到资源的线程，此时 node 被唤醒，很可能是 head 用完资源来唤醒自己的
                if (p == head) {
					// 尝试获取资源
                    int r = tryAcquireShared(arg);
					// 成功
                    if (r >= 0) {
						// 将 head 指向自己，还有剩余资源可以再唤醒之后的线程
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
						// 如果等待过程中被打断过，此时将中断补上。
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
				// 判断状态，寻找安全点，进入 waiting 状态，等着被 unpark() 或 interrupt()
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
	
    private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

	/**
     * 获取排他锁
	 * 1.尝试获取（调用 tryAcquire 更改状态，需要保证原子性）
	 * 2.如果获取不到，将当前线程构造成节点 Node 并加入 sync 队列
	 * 3.再次尝试获取，如果没有获取到那么将当前线程从线程调度器上摘下，进入等待状态
     */
    public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
	
	/**
     * 释放排他锁
	 * 1.尝试释放状态
	 * 2.唤醒当前节点的后继节点所包含的线程
     */
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
			// 找到头结点
            Node h = head;
            if (h != null && h.waitStatus != 0)
				// 唤醒等待队列里的下一个线程
                unparkSuccessor(h);
            return true;
        }
        return false;
    }
	
	/**
     * 获取共享锁
     */
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }
	
	/**
     * 释放共享锁
     */
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }
	
	/**
     * 这几个判断声明都是一样的。
	 * 都是父类中只有定义，在子类中实现。
	 * 子类根据功能需要的不同，有选择的对需要的方法进行实现。
	 * 父类中提供一个执行模板，但是具体步骤留给子类来定义，不同的子类有不同的实现
     */
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

	// 尝试去释放指定量的资源
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

	/**
     * 在排它模式下，状态是否被占用。
     */
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

	/**
     * 提供获取状态能力，当然在无法获取状态的情况下会进入 sync 队列进行排队。
	 * 类似 acquire，但是和 acquire 不同的地方在于它能够在外界对当前线程进行中断的时候提前结束获取状态的操作。
	 * 换句话说，就是在类似 synchronized 获取锁时，外界能够对当前线程进行中断，并且获取锁的这个操作能够响应中断并提前返回。
	 * 1.检测当前线程是否被中断
	 * 2.尝试获取状态
	 * 3.构造节点并加入sync队列
	 * 4.中断检测
     */
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }

    public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)
            doAcquireSharedInterruptibly(arg);
    }

    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
            doAcquireSharedNanos(arg, nanosTimeout);
    }

    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    public final boolean hasContended() {
        return head != null;
    }

    public final Thread getFirstQueuedThread() {
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }

    private Thread fullGetFirstQueuedThread() {
        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) ||
            ((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null))
            return st;

        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)
                firstThread = tt;
            t = t.prev;
        }
        return firstThread;
    }

    public final boolean isQueued(Thread thread) {
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }

    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }

    public final boolean hasQueuedPredecessors() {
        Node t = tail;
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }

    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }

    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }

    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    public String toString() {
        int s = getState();
        String q  = hasQueuedThreads() ? "non" : "";
        return super.toString() +
            "[State = " + s + ", " + q + "empty queue]";
    }
	
    final boolean isOnSyncQueue(Node node) {
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        if (node.next != null)
            return true;
        return findNodeFromTail(node);
    }

    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }

    final boolean transferForSignal(Node node) {
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        Node p = enq(node);
        int ws = p.waitStatus;
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

    final boolean transferAfterCancelledWait(Node node) {
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }
        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    public class ConditionObject implements Condition, java.io.Serializable {
        private static final long serialVersionUID = 1173984872572414699L;
        private transient Node firstWaiter;
        private transient Node lastWaiter;

        public ConditionObject() { }

        private Node addConditionWaiter() {
            Node t = lastWaiter;
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        private void doSignal(Node first) {
            do {
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                first.nextWaiter = null;
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
        }

        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }

        public final void signal() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignal(first);
        }

        public final void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }

        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted())
                    interrupted = true;
            }
            if (acquireQueued(node, savedState) || interrupted)
                selfInterrupt();
        }

        private static final int REINTERRUPT =  1;
        private static final int THROW_IE    = -1;

        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }

        private void reportInterruptAfterWait(int interruptMode)
            throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }

        public final void await() throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }

        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime();
        }

        public final boolean awaitUntil(Date deadline)
                throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        public final boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            return sync == AbstractQueuedSynchronizer.this;
        }

        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }
    }

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("next"));

        } catch (Exception ex) { throw new Error(ex); }
    }

    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                                        expect, update);
    }

    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}
