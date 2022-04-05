# Tomcat

## session 共享方案

### 使用 Cookie 实现

- 将系统用户的 Session 信息加密、序列化后，以 Cookie 的方式， 统一种植在根域名下
- 缺点

	- 占用带宽，最主要的是保存在客户端，存在很大的安全隐患

### 使用 Nginx 中的 ip 绑定策略

- 配置 ip_hash
- 缺点

	- 无法负载均衡

### 使用数据库同步 session

- 将 session 数据存到数据库中
- 缺点

	- Session 的并发读写能力取决于数据库的性能，对数据库的压力大，同时需要自己实现 Session 淘汰逻辑，以便定时从数据表中更新、删除 Session 记录，当并发过高时容易出现死锁

### 使用 token(JWT) 代替 session

- JWT 基本流程

	- 客户端通过用户名和密码登录服务器
	- 服务端对客户端身份进行验证
	- 服务端对该用户生成 Token，返回给客户端
	- 客户端将 Token 保存到本地浏览器，一般保存到 cookie 中
	- 客户端发起请求，需要携带该 Token
	- 服务端收到请求后，首先验证 Token，之后返回数据

### 使用 tomcat 内置的 session 同步

### Tomcat 通过 Redis 实现 session 共享


- 当客户端访问 Nginx 服务器时，Nginx 负载均衡会自动将请求转发到 Tomcat1 节点或 Tomcat2 节点服务器，以减轻 Tomcat 压力，从而达到 Tomcat 集群化部署，为了使各 Tomcat 之间共享同一个 Session，可以采用 Redis 缓存服务来集中管理 Session 存储。Nginx 实现负载均衡，并使用 Redis 实现 session 共享。
- 可以给这个 redis 节点配置一个从节点,采用 redis 主从模式,连接 redis 的 master 节点，redis 默认不支持主主模式

### Spring-Session + Redis 实现

- 当 Web 服务器接收到请求后，请求会进入对应的 Filter 进行过滤，将原本需要由 Web 服务器创建会话的过程转交给 Spring-Session 进行创建。Spring-Session 会将原本应该保存在 Web 服务器内存的 Session 存放到 Redis 中。然后 Web 服务器之间通过连接 Redis 来共享数据，达到 Sesson 共享的目的。

