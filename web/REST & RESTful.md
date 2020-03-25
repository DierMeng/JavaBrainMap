# REST & RESTful

## JAX-RS2

### JAX-RS: Java API for RESTful Web Services，Java 领域的 REST 式的 web 服务的标准规范，是使用 Java 完成 REST 服务
的基本约定

JAX-RS 规范并不等于 REST 架构风格本身，REST 的内涵要比 JAX-RS 广泛得多。

- 优势

	- 完全基于 POJO
	- 很容易做单元测试
	- 将 HTTP 作为一种应用协议而不是可替代的传输协议
	- 优秀的 IDE 集成

- 目标

	- 基于 POJO
	- 以 HTTP 为中心
	- 格式独立性
	- 容器独立性
	- 内置于 Java EE

- 元素

	- 资源类
	- 根资源类
	- 请求方法标识符
	- 资源方法
	- 子资源标识符
	- 子资源方法
	- Provider
	- Filter
	- Entity Interceptor
	- Invocation
	- WebTarget
	- Link

### REST（Representational State Transfer），表述性状态转移。跨平台、跨语言。

REST 是一种分布式应用的架构风格，也是一种大流量分布式应用的设计方法论。

- 基本概念

	- 表述性状态是指资源数据在某个瞬时的状态快照，表述状态具有描述性,包括资源数据的内容、表述格式（比如 XML、JSON、Atom）等信息。所以 REST 是一种架构风格（既不是标准也不是协议），在这种架构风格中,对象被视为一种资源（resource），通常使用概念清晰的名词命名。

- 特点

	- 客户端-服务器的
	- 无状态的
	- 可缓存的
	- 统一接口
	- 分层系统
	- 按需编码

- 基本实现形式

	- HTTP + URI + （XML、JSON等）
	- HTTP 协议和 URI 用于统一接口和定位资源

### 风格

- RESTful

	- REST 服务

		- RESTful 对应的中文是 REST 式的,RESTful Web Service 的准确翻译是 REST 式的 Web 服务，我们通常简称为 REST 服务。

	- ROA

		- Resource- Oriented Architecture，面向资源的架构
		- 主要特点

			- 方法信息存在于 HTTP 协议的方法中（比如 GET、PUT），作用域存在于 URI 中。

- RPC，Remote Procedure Call，远程过程调用

	- XML-RPC
	- 大 Web 服务

		- SOAP

- MVC

	- 与 REST 不互斥，MVC 早就支持了 REST

## REST API

### 统一接口

### 资源定位

### 传输格式

### 连通性

### 处理响应

### 内容协商

## REST 请求处理

## REST 服务与异步

## REST 客户端

## REST 测试

## 微服务

## 容器化

## JAX-RS 调优

## REST 安全

*XMind: ZEN - Trial Version*