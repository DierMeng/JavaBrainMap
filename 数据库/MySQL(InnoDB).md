# https://pic.imgdb.cn/item/624c1415239250f7c5a44da7.png
MySQL(InnoDB)

## 一、索引（B+树）

索引是一种数据结构，用于加快mysql获取数据的速度；

常见索引模型：哈希表、有序数组、搜索树。

索引类型：主键索引（存储整行数据）、非主键索引（主键的值，需要回表两次查询）
### 参考隔壁《MySQL 索引总结（详细版）》

## 二、事务

### 事务的概念

- 据库事务是数据库系统执行过程中的一个逻辑处理单元，保证一组数据库操作要么全部成功（提交），要么全部失败（回滚）。

### 事务的特性

- 原子性（Atomic）

  事务中包含的操作被看作一个整体的业务单元，这个业务单元中的操作要么全部成功，要么全部失败，不会出现部分失败、部分成功的场景。
- 一致性（Consistency）

  事务在完成时，必须使所有的数据都保持一致状态，在数据库中所有的修改都基于事务，保证了数据的完整性。
- 隔离性（Isolation）

	- 必须要掌握的知识点
	- 一个事务的修改在最终提交前，对其他事务是不可见的。主要针对并发场景。

- 持久性（Durability）

  事务结束后，所有的数据会固化到一个地方，如保存到磁盘当中，即使断电重启后也可以提供给应用程序访问。
- 底层实现

	- 利用 undo log 保证原子性，记录了需要回滚的日志信息。

		- 事务回滚时会撤销已经执行成功的 SQL。

	- 由内存 + redo log 来保证

		- MySQL 修改数据同时在内存和 redo log 记录这次操作，事务提交的时候通过 redo log 刷盘，宕机的时候可以从 redo log 恢复。

	- 利用锁和 MVCC 机制保证隔离性
	- 通过原子性、持久性、隔离性来保证一致性

		- 一般由代码层面来保证。

### 事务的隔离级别

- 并发事务的问题

	- 数据丢失

		- 一个事务的更新被另一个事务的更新所覆盖。

			- 由于事务A更新失败回滚，导致事务B更新的数据被覆盖掉，造成数据丢失

	- 脏读

		- 一个事务读到另一个事务没有提交的数据。

			- 由于事务A更新失败回滚，导致事务B读取的数据为脏数据

	- 不可重复读
	- 幻读

- 隔离级别

	- 未提交读（Read Uncommited）：脏读

		- 可以读取未提交记录。此隔离级别，不会使用，忽略。

	- 读写提交（Read Committed，RC）：幻读

		- 一个事务只能读取另外一个事务己经提交的数据，
不能读取未提交的数据。
		- 幻读的意思其实就是读写提交会产生不可重复读的问题

	- 可重复读（Repeatable Read，RR）（默认的隔离级别）

		- 字面的意思就是必须等上一个事务提交才能进行当前事务的读取操作，保证数据正确性。
		- RR 隔离级别保证对读取到的记录加锁 (记录锁)，同时保证对读取的范围加锁，新的满足查询条件的记录不能够插入 (间隙锁)，不存在幻读现象。


		- 在这里里面也有一个「幻读」的概念，不过可重复读产生幻读的现象不属于数据库存储的值，多半是统计值或者计算值。
		- 底层实现

			- 利用间隙锁，防止幻读的出现，保证了可重复读
			- MVCC 的快照生成时机不同

	- 串行化（Serializable）

		- 从 MVCC 并发控制退化为基于锁的并发控制。不区别快照读与当前读，所有的读操作均为当前读，读加读锁 (S锁，共享锁)，写加写锁 (X锁，排它锁)。
		- Serializable 隔离级别下，读写冲突，因此并发度急剧下降，在 MySQL/InnoDB 下不建议使用。

### 事务的底层日志

- undo log

	- undo log 是回滚日志，提供回滚操作。
	- undo 用来回滚行记录到某个版本。undo log 一般是逻辑日志，根据每行记录进行记录。
	- 主要用来主从复制和恢复数据用。

- redo log

	- redo log 是重做日志，提供前滚操作
	- redo log 通常是物理日志，记录的是数据页的物理修改，而不是某一行或某几行修改成怎样怎样，它用来恢复提交后的物理数据页(恢复数据页，且只能恢复到最后一次提交的位置)。

- MySQL 如何解决 undo log 和 redo log 的原子一致性

	- MySQL 的内部 XA 事务，即两阶段提交

		- prepare：写入redo log，并将回滚段置为 prepared 状态，此时 binlog 不做操作。
		- commit：InnoDB 释放锁，释放回滚段，设置提交状态，写入 binlog，然后存储引擎层提交。

- MySQL 崩溃恢复

	- 扫描最后一个 Binlog 文件，提取其中的 xid；
	- InnoDB 维持了状态为 Prepare 的事务链表，将这些事务的 xid 和 binlog 中记录的 xid 做比较，如果在 binlog 中存在，则提交，否则回滚事务。

### 大事务（长时间执行的事务）的解决方案

- 带来的问题

	- 造成大量的阻塞和锁超时，容易造成主从延迟
	- 大事务如果执行失败，回滚也会很耗时

- 排查大事务的方式

	- 监控 information_schema.Innodb_trx 表，设置长事务阈值，超过就报警或者杀进程
	- select * from information_schema.innodb_trx where TIME_TO_SEC(timediff(now(),trx_started))>60;
	- 生产中，会将监控大事务的语句，配成定时脚本，进行监控。

- 结合业务场景，优化 SQL，将一个大事务拆成多个小事务执行，或者缩短事务执行时间即可。

### 数据库宕机重启，事务丢失的情况

- innodb_flush_log_at_trx_commit & sync_binlog

	- 默认值为 1 & 0

		- 每次事务提交时都将 redo log 直接持久化到磁盘.但是MySQL不控制binlog的刷新，由操作系统自己控制它的缓存的刷新。
		- 一旦系统宕机，在 binlog_cache 中的所有 binlog 信息都会被丢失。

	- 双 1 配置

		- 每次事务提交时都将 redolog 直接持久化到磁盘，binlog 也会持久化到磁盘。
		- 性能是最差的，适合金融系统

	- 2 & 0 配置

		- 每次事务提交时，只是把 redolog 写到 OS cache，隔一秒，MySQL 主动将 OS cache 中的数据批量 fsync。
		- 一旦系统宕机，在 binlog_cache 中的所有 binlog 信息都会被丢失。
		- 相对性能最好的一套配置

## 锁

### 共享锁

### 排它锁

### 乐观锁

### 悲观锁

### GAP 锁

### 2PL（二阶段锁）：

- 加锁阶段
- 解锁阶段
- 保证加锁阶段与解锁阶段不相交。

### 死锁

死锁的发生与否，并不在于事务中有多少条 SQL 语句，死锁的关键在于：两个或以上的 Session 加锁的顺序不一致。
## MySQL 日志体系

### binlog（归档日志，逻辑日志，二进制）

- Server 层（缓存、连接器、分析器、优化器、执行器）记录的操作日志
- 记录的是这个更新语句的原始逻辑

	- 追加写，是指一份写到一定大小的时候会更换下一个文件，不会覆盖。

- 底层相关概念

	- binlog 是记录所有数据库表结构变更（例如 CREATE、ALTER TABLE 等）以及表数据修改（INSERT、UPDATE、DELETE 等）的二进制日志。
	- binlog 不会记录 SELECT 和 SHOW 这类操作，因为这类操作对数据本身并没有修改，可以通过查询通用日志来查看 MySQL 执行过的所有语句。
	- binlog 组成

		- 索引文件（文件名后缀为.index）用于记录哪些日志文件正在被使用
		- 日志文件（文件名后缀为.00000*）记录数据库所有的 DDL 和 DML（除了数据查询语句）语句事件。

	- 用途

		- 恢复
		- 复制

			- 主库有一个 log dump 线程，将 binlog 传给从库
			- 从库有两个线程，一个 I/O 线程，一个 SQL 线程

				- I/O 线程读取主库传过来的 binlog 内容并写入到 relay log
				- SQL 线程从 relay log 里面读取内容，写入从库的数据库。

		- 审计

			- 防 SQL 注入

	- 日志查看

		- mysqlbinlog -vv mysql-bin.000001 

	- binlog 日志格式

		- statement

			- 记录的是修改 SQL 的语句

		- row（推荐）

			- 记录的是每行实际数据的变更

		- mixed

			- 前两者的混合

### redo log（重做日志文件，提供前滚操作）

- InnoDB 存储引擎层的日志，用于记录事务操作的变化，记录的是数据修改之后的值，不管事务是否提交都会记录下来。

	- 可应对断电时候的数据恢复，主从复制搭建
	- 数据库进行异常重启的时候，可以根据redo log日志进行恢复

- 在一条更新语句进行执行的时候，InnoDB 引擎会把更新记录写到 redo log 日志中，然后更新内存，此时算是语句执行完了，然后在空闲的时候或者是按照设定的更新策略将 redo log中的内容更新到磁盘中

	- 先写日志，在更新硬盘
	- redo log 日志的大小是固定的，即记录满了以后就从头循环写

- 记录事务执行后的状态，用来恢复未写入 data file 的已成功事务更新的数据。

### undo log（回滚日志）

- 用来回滚行记录到某个版本。
- 一般是逻辑日志，根据每行记录进行记录。
- 保存了事务发生之前的数据的一个版本，可以用于回滚（MVCC）
- 用于记录事务开始前的状态，用于事务失败时的回滚操作

## binlog

## MVCC（Multi-Version Concurrency Control，基于多版本的并发控制协议）：读不加锁，读写不冲突。

### 快照读（snapshot read）

- 简单的select操作，属于快照读，不加锁。

### 当前读（current read）

- 特殊的读操作，插入/更新/删除操作，属于当前读，需要加锁。

	- select * from table where ? lock in share mode;（共享锁）
	- select * from table where ? for update;（排它锁）
	- insert into table values (…);
	- update table set ? where ?;
	- delete from table where ?;

## 一条 SQL 的生命周期

### 词法解析、语法解析、权限检查、查询优化、SQL执行等

### where 的提取

- Index Key，索引的起始范围

	- Index First Key
	- Index Last Key

- Index Filter，索引起始范围之外依然满足查询条件的查询范围，逐个索引列检索
- Table Filter，不属于索引列的查询条件

### 执行计划

- 主键扫描
- 唯一键扫描
- 范围扫描
- 全表扫描

### inner join、left join、right join 的区别

- inner join（等值联接）：只返回两个表中联接字段相等的记录。
- left join（左联接）：返回左表中的所有记录以及和右表中的联接字段相等的记录。
- right join（右联接）：返回右表中的所有记录以及和左表中的联接字段相等的记录。

## 表

### 分类

- 堆表：无序存储
- 聚簇索引表：按照主键顺序存储

### MySQL 大表分页

- 分页查询

	- select * from order where user_id = xxx and 【其它业务条件】 order by created_time, id limit offset, pageSize

		- 定位到 offset 的成本过高，未能充分利用索引的有序性

	- select * from order where id > 'pre max id' order by id limit 50
	- 行比较

		- select * from order where user_id = xxx and 【其它业务条件】 and (created_time > 'created_time of latest recode' or (created_time = 'created_time of latest recode' and id > 'id of latest recode'))  order by created_time, id limit pageSize

- 索引（b+ tree）的特点在于，数据是有序的，虽然找到第 N 条记录的效率比较低，但找到某一条数据在索引中的位置，其效率是很高的

### 线上修改表结构的方案

- ALTER TABLE table_name CHANGE（不推荐）

	- 修改表的过程中，对绝大部分操作，原表可读，也可以写。
	- 但是对于这个要修改的列，不支持并发的 DML 操作

- 借助第三方工具

	- 支持在线修改表结构，能够让你在执行 ALTER 操作的时候，表不会阻塞

		- pt-online-schema-change

			- 创建一个新的表，表结构为修改后的数据表，用于从源数据表向新表中导入数据
			- 创建触发器，用于记录从拷贝数据开始之后，对源数据表继续进行数据修改的操作记录下来，用于数据拷贝结束后，执行这些操作，保证数据不会丢失。
			- 拷贝数据，从源数据表中拷贝数据到新表中。
			- rename 源数据表为 old 表，把新表 rename 为源表名，并将 old 表删除。
			- 删除触发器

		- gh-ost（GitHub 旗下）

- 改从库表结构，然后主从切换

	- 主从切换过程中可能会有数据丢失的情况

## InnoDB 架构

### 逻辑架构

- 主图就是逻辑架构

### https://pic.imgdb.cn/item/624c5bf1239250f7c53cfc23.png
内存和磁盘架构

- 执行器更新内存数据
- 写 redo log 日志
- 准备提交事务，写入磁盘
- 准备提交事务，binlog 写入磁盘
- 写入 commit 标记

