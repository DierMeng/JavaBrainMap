# Spring Cloud 与 Docker 微服务架构实战

## 使用 Zuul 构建微服务网关

## 使用 Spring Cloud Config 统一管理微服务配置

### 为什么要统一管理微服务配置

- 集中管理配置
- 不同环境，不同配置
- 运行期间可动态调整

### Spring Cloud Config 简介

### 编写 Config Server

### 编写 Config Client

### Config Server 的 Git 仓库配置详解

- 占位符支持

	- {application}
	- {profile}
	- {label}

- 模式匹配

	- 通配符

- 搜索目录

	- search-path

- 启动时加载配置文件

### Config Server 的健康状况指示器

### 配置内容的加解密

- 安装 JCE
- Config Server 加解密端点
- 对称加密
- 存储加密的内容
- 非对称加密

### 使用「/refresh」端点手动刷新配置

### 使用 Spring Cloud Bus 自动刷新配置

- Spring Cloud Bud 简介
- 实现自动刷新
- 局部刷新
- 架构改进
- 跟踪总线事件

### Spring Cloud Config 与 Eureka 配合使用

### Spring Clkoud Config 的用户认证

### Config Server 的高可用

- Git 仓库的高可用

	- 子主题 1

- RabbitMQ 的高可用
- Config DServer 自身的高可用

## 使用 Spring Cloud Sleuth 实现微服务跟踪

### 为什么要实现微服务跟踪

- 微服务之间通过网络进行通信
- 跟踪每个请求，了解请求经过哪些微服务、请求耗费时间、网络延迟、业务逻辑耗费时间等指标
- 分布式跟踪的解决方案

### Spring Cloud Sleuth 简介

- span（跨度）

	- 基本工作单元，初始化 span 被称为「root span」

- trace（跟踪）

	- 一组共享「root span」的 span 组成的树状结构称为 trace

- annotation（标注）

	- CS（Client Sent 客户端发送）：客户端发起一个请求，描述了 span 的开始
	- SR（Server Received 服务器端接收）

		- 服务器端获得请求并准备处理它
		- 网络延迟

			- SR - CS 时间戳

	- SS（Server Sent 服务器端发送）

		- 完成请求处理（响应发回客户端）
		- 服务器端处理请求所需的时间

			- SS - SR 时间戳

	- CR（Client Received 客户端接收）

		- span 结束的标识。客户端成功接收到服务器端的响应
		- 客户端发送请求到服务器响应的时间

			- CR - CS 时间戳

### 整合 Spring Cloud Sleuth

### Spring Cloud Sleuth 与 ELK 配合使用

### Spring Cloud Sleuth 与 Zipkin 配合使用

- Zipkin 简介

	- 收集系统的时序数据，追踪微服务架构的系统延时等问题

- 编写 Zipkin Server

	- 界面查询条件的含义

		- Service Name

			- 服务名称，各个微服务 spring.application.name 的值

		- span 的名称

			- all 标识所有 span，也可选择指定 span

		- Start time、End time

			- 用于指定起始时间和截止时间

		- Duration

			- 持续时间，span 从创建到关闭所经历的时间

		- Limit

			- 表示查询几条数据

		- Annotations Query

			- 自定义查询条件

- 微服务整合 Zipkin
- Zipkin 与 Eureka 配合使用
- 使用消息中间件收集数据

	- 优势

		- 微服务与 Zipkin Server 解耦
		- 某些场景可以解决 Zipkin Server 与微服务网络不通的情况

- 使用 Elasticsearch 存储跟踪数据
- 依赖关系图

## Spring Cloud 常见问题与总结

### Eureka 常见问题

- Eureka 注册服务慢
- 已停止的微服务节点注销慢或不注销
- 如何自定义微服务的 Instance ID
- Eureka 的 UNKNOWN 问题总结与解决

### 整合 Hystrix 后首次请求失败

- 原因分析
- 解决方案

### Turbine 聚合的数据不完整

### Spring Cloud 各组件超时

- RestTemplate 的超时
- Ribbon 的超时
- Feign 的超时
- Hystrix 的超时
- Zuul 的超时

### Spring Cloud 各组件重试

## Doeker 入门

## 将微服务运行在 Docker 上

## 使用 Docker Compare 编排微服务

## 使用 Hystrix 实现微服务的容错处理

## 使用 Feign 实现声明式 REST 调用

## 使用 Ribbon 实现客户端侧负载均衡

## 微服务注册与发现

### // TODO

## 实战基础相关概念

### 服务提供者：服务的被调用方，为其他服务提供服务的服务

### 服务消费者：服务的调用方，依赖其它服务的服务

### @SpringBootApplication 注解

- @Configuration
- @EnableAutoConfiguration
- @ComponentScan

### Spring Initializr 的四种创建方式

- （推荐）网页：https://start.spring.io/
- Spring Tool Suite
- Intellij IDEA 自带 spring.io
- Spring Boot CLI

### @Bean

- 方法注解，作用是实例化一个 Bean 并使用该方法的名称命名

### Spring Boot Actuator，主要就是端点，http(s)://{ip/域名}:{port}/{endpoint}

- autoconfig

	- 显示自动配置信息

- beans

	- 显示应用程序上下文所有的 Spring bean

- configprops

	- 显示所有 @ConfigurationProperties 的配置属性列表

- dump

	- 显示线程活动的快照

- env

	- 显示应用的环境变量

- health

	- 显示应用程序的健康指标

- info

	- 显示应用的信息

- mappings

	- 显示所有的 @RequestMapping 的路径列表

- metrics

	- 显示应用的度量标准信息

- shutdown

	- 关闭应用

- trace

	- 显示跟踪信息，默认最近 100 个 HTTP 请求

### RestTemplate 硬编码存在的问题

- 适用场景局限：比如服务提供者的 IP 发生变化会影响消费者的服务地址请求配置。
- 无法动态伸缩：生产环境一个微服务可能部署多个实例来进行负载均衡和容灾，但是硬编码满足这种需求很繁琐。

## Spring Cloud 简介

### 概念

- 基于 Spring Boot 构建，用于快速构建分布式系统的通用模式的工具集

### Spring Cloud 特点

- 约定优于配置

  关于「约定优于配置」的概念可以参考一下老四的这片《阿里巴巴Java开发手册第三章-单元测试篇》文章。里面有相关的解读。http://www.glorze.com/437.html

	- 子主题 1

- 适用于各种服务器环境。云服务器、Docker 等
- 隐藏组件复杂性，提供声明式、无 XML 的配置方式
- 开箱即用，快速启动
- 轻量级组件：Eureka、Zuul
- 完整的微服务体系

	- 配置管理
	- 服务发现
	- 断路器
	- 微服务网关

- 选型中立、丰富

	- Eureka
	- ZooKeeper
	- Consul

- 灵活，各个部分均为解耦设计

### Spring Cloud 版本

- SR 代表「Service Release」，一般代表 Bug 修复，后面代表的是第几次的版本 Bug 修复。
- 发行版均用伦敦地铁站命名

	- Greenwich
	- Finchley
	- Edgware
	- Dalston
	- Camden
	- Brixton
	- Angle

## 微服务架构概述

### 单体应用

- 定义

	- 一个归档包（例如 war 格式）包含所有功能的应用程序

- 存在的问题

	- 复杂性高

	  模块边界模糊，依赖关系不清晰。

	- 技术债务

		- 不坏不修

	- 部署频率低

	  全量部署的方式耗时长、影响范围大、风险高。

	- 可靠性差

		- 一个 Bug 可能导致整个应用崩溃

	- 扩展能力受限

		- 密集型应用模块需要强劲的 CPU
		- IO密集型的需要更大的内存

	- 阻碍技术创新

		- 重构或者改造成本大，也基本无法跨语言

### 微服务

- 定义

	- 将一个单一应用程序开发为一组小型服务的方法，每个服务运行在自己的进程中，服务间采用轻量级通信机制（通常用 HTTP 资源 API）。这些服务围绕业务能力构建并且可通过全自动部署机制独立部署。这些服务共用一个最小型的集中式管理，服务可用不同的语言开发，使用不同的数据存储技术。

- 特性

	- 每个微服务可独立运行在自己的进程里
	- 一系列微服务共同构建整个系统
	- 每个服务为独立的业务开发，分模块或者业务
	- 通过轻量级通信机制进行通信，如 Restful API。
	- 可以使用不同的语言和数据库
	- 全自动部署机制

- 优势

	- 易于开发和维护
	- 单个微服务启动较快
	- 局部修改容易部署
	- 技术站不受限
	- 按需伸缩

- 存在的问题与挑战

	- 运维要求较高
	- 分布式固有的复杂性

		- 系统容错
		- 网络延迟
		- 分布式事务

	- 接口调整成本高
	- 重复劳动

- 设计原则

	- 单一职责原则

	  这里其实指的是设计模式中的七个面向对象设计（可以先短暂的参考：https://git.io/fjr9M）原则之一，而且单一职责原则属于「SOLID 原则」之一，这个 SOLID 代表的就是单一职责原则、开放-封闭原则、里氏里氏代换原则、接口隔离原则以及依赖倒转原则的组合，关于这几个基本的面向对象设计原则也可以简单的参考一下老四写的《设计模式系列》。http://www.glorze.com/tag/%e8%ae%be%e8%ae%a1%e6%a8%a1%e5%bc%8f

	- 服务自治原则

		- 服务是独立的业务单元

	- 轻量级通信机制

		- REST（Representational State Transfer，表现层状态转换）
		- AMQP（Advanced Message Queuing Protocol，一个提供统一消息服务的应用层标准高级消息队列协议）
		- STOMP（Streaming Text Orientated Message Protocol，流文本定向消息协议）
		- MQTT（Message Queuing Telemetry Transport，消息队列遥测传输）

	- 微服务力度

		- 领域驱动设计
		- 康威定律：设计系统的架构受制于产生这些设计的组织的沟通结构。

		  系统设计(产品结构)等同组织形式，每个设计系统的组织，其产生的设计等同于组织之间的沟通结构。

			- 组织沟通方式会通过系统设计表达出来
			- 时间再多一件事情也不可能做的完美，但总有时间做完一件事情
			- 线型系统和线型组织架构间有潜在的异质同态特性
			- 大的系统组织总是比小系统更倾向于分解

- 服务架构

	- 自动化部署工具，如持续集成架构

		- IaaS：基础设施服务，Infrastructure-as-a-service
		- PaaS：平台服务，Platform-as-a-service
		- SaaS：软件服务，Software-as-a-service

	- 开发框架

		- Spring Cloud
		- Dubbo
		- Dropwizard
		- Armada

	- 运行平台

		- 直接部署
		- Docker

