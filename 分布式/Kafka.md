# Kafka

## 队列的普遍作用

### 解耦

### 冗余

### 扩展性

### 灵活性 & 峰值处理能力

### 可恢复性

### 顺序保证

### 缓冲

### 异步通信

## Kafka 架构

### Broker

- Kafka 集群包含一个或多个服务器，这种服务器被称为 broker

### Topic

- 每条发布到 Kafka 集群的消息都有一个类别，这个类别被称为 Topic。
- Topic 在逻辑上可以被认为是一个 queue，每条消费都必须指定它的 Topic
- 为了使 Kafka 的吞吐率可以线性提高，物理上把 Topic 分成一个或多个 Partition，每个 Partition 在物理上对应一个文件夹，该文件夹下存储这个 Partition 的所有消息和索引文件。

### Partition

- 物理上的概念，每个 Topic 包含一个或多个 Partition。
- log entry

	- 4字节整型数值，message length
	- 1个字节的「magic value」
	- 4个字节的CRC校验码
	- 消息体
	- log entry 并非由一个文件构成，而是分成多个segment，每个 segment 以该 segment 第一条消息的 offse t命名并以「.kafka」为后缀。
	- 还有一个索引文件，它标明了每个 segment 下包含的 log entry 的 offset 范围

### Producer

- 负责发布消息到 Kafka broker，推模式
- Producer 发送消息到 broker 时，会根据 Paritition 机制选择将其存储到哪一个 Partition。如果 Partition 机制设置合理，所有消息可以均匀分布到不同的 Partition 里，这样就实现了负载均衡。

### Consumer

- 消息消费者，向 Kafka broker 读取消息的客户端，拉模式
- 使用 Consumer high level API 时，同一 Topic 的一条消息只能被同一个 Consumer Group 内的一个 Consumer 消费，但多个 Consumer Group 可同时消费这一消息。

### Consumer Group

- 每个 Consumer 属于一个特定的 Consumer Group

### 一个典型的 Kafka 集群中包含若干 Producer，若干 broker（Kafka 支持水平扩展，一般 broker数量越多，集群吞吐率越高），若干Consumer Group，以及一个 Zookeeper 集群。Kafka 通过 Zookeeper 管理集群配置，选举 leader，以及在 Consumer Group 发生变化时进行 rebalance。Producer 使用 push 模式将消息发布到 broker，Consumer 使用 pull 模式从 broker 订阅并消费消息。 　　

### Kafka 如何保证 Exactly once

消息正好传输一次

- 当 Producer 向 broker 发送消息时，一旦这条消息被 commit，它就不会丢。但是如果 Producer发送数据给 broker 后，遇到网络问题而造成通信中断，那 Producer 就无法判断该条消息是否已经 commit。虽然 Kafka 无法确定网络故障期间发生了什么，但是 Producer 可以生成一种类似于主键的东西，发生故障时幂等性的重试多次，这样就做到了 Exactly once。

## Kafka Consumer 设计

### High Level Consumer

实现传统 Message Queue 消息只被消费一次的语义

- Kafka 并不删除已消费的消息，为了实现传统 Message Queue 消息只被消费一次的语义，Kafka 保证每条消息在同一个 Consumer Group里只会被某一个 Consumer 消费。
- Kafka 还允许不同 Consumer Group 同时消费同一条消息
- Kafka 对消息的分配是以 Partition 为单位分配的，而非以每一条消息作为分配单元。

	- 无法保证同一个 Consumer Group 里的 Consumer 均匀消费数据
	- 但是是每个 Consumer 不用都跟大量的 Broker 通信，减少通信开销，同时也降低了分配难度，实现也更简单。
	- 同一个 Partition 里的数据是有序的，这种设计可以保证每个 Partition 里的数据可以被有序消费。

## 分支主题 4

