# Fork/Join 框架，分而治之，工作窃取

## 操作

### fork 操作：把任务分成更小的任务

### join 操作：一个任务等待它创建的任务的结束

## 局限

### 任务只能使用 fork() 和 join() 操作，作为同步机制。

### 任务不应该执行 I/O 操作，如读或写数据文件。

### 任务不能抛出检查异常，它必须包括必要的代码来处理它们。

## 核心类

### ForkJoinPool，继承 AbstractExecutorService 抽象类

- 实现 work-stealing（工作窃取） 算法。它管理工作线程和提供关于任务的状态和它们执行的信息。

### ForkJoinTask，实现 Future 接口

- 它是 ForkJoinPool 中执行的任务的基类。它提供在任务中执行 fork() 和 join() 操作的机制，并且这两个方法控制任务的状态。通常，为了实现 Fork/Join 任务，要实现它两个子抽象类。

	- RecursiveAction，无返回结果的任务
	- RecursiveTask，存在返回结果的任务

## 分支主题 4

*XMind: ZEN - Trial Version*