# 深入浅出 Docker

## 第一部分 Docker 概览

### 第1章　容器发展之路

### 第2章　走进Docker

- Docker——简介

	- Docker 是一种运行于 Linux 和 Windows 上的软件，用于创建、管理和编排容器。Docker 是在 GitHub 上开发的 Moby 开源项目的一部分。

- Docker 运行时与编排引擎

	- Docker 引擎是用于运行和编排容器的基础设施工具。

### 第3章　Docker 安装

### 第4章　纵观 Docker

- 运维视角

	- 镜像

		- 在 Docker 世界中，镜像实际上等价于未运行的容器。

		- 镜像包含了基础操作系统，以及应用程序运行所需的代码和依赖包。

		- Docker 的每个镜像都有自己的唯一 ID。用户可以通过引用镜像的 ID 或名称来使用镜像。如果用户选择使用镜像 ID，通常只需要输入 ID 开头的几个字符即可——因为 ID 是唯一的，Docker 知道用户想引用的具体镜像是哪个。

	- 容器

		- 使用 docker container run 命令从镜像来启动容器

	- 连接到运行中的容器

		- 执行 docker container exec 命令，可以将 Shell 连接到一个运行中的容器终端。

		- 通过 docker container stop 和 docker container rm 命令来停止并杀死容器。

		- 通过运行 docker container ls -a 命令，让 Docker 列出所有容器，甚至包括那些处于停止状态的。

- 开发视角

	- Dockerfile 是一个纯文本文件，其中描述了如何将应用构建到 Docker 镜像当中。

	- 使用 docker image build 命令，根据 Dockerfile 中的指令来创建新的镜像。

## 第二部分 Docker 技术

### 第5章　Docker 引擎

- 简介

	- Docker 引擎是用来运行和管理容器的核心软件。

	- 基于开放容器计划（OCI）相关标准的要求，Docker 引擎采用了模块化的设计原则，其组件是可替换的。

	- Docker 引擎主要的组件构成

		- Docker客户端（Docker Client）

		- Docker守护进程（Docker daemon）

		- containerd

		- runc

- https://pic1.imgdb.cn/item/6334421016f2c2beb157ed33.jpg
详解

	- runc

		- runc 是 OCI 容器运行时规范的参考实现，生来只有一个作用——创建容器，实质上就是一个独立的容器运行时工具。

	- https://pic1.imgdb.cn/item/63353a4916f2c2beb1265faf.jpg
containerd

		- 主要任务是容器的生命周期管理——start | stop | pause | rm ....

	- shim

		- shim是实现无daemon的容器不可或缺的工具。

		- containerd 指挥 runc 来创建新容器。事实上，每次创建容器时它都会 fork 一个新的 runc 实例。不过，一旦容器创建完毕，对应的 runc 进程就会退出。

		- 一旦容器进程的父进程 runc 退出，相关联的 containerd-shim 进程就会成为容器的父进程。

			- 保持所有 STDIN 和 STDOUT 流是开启状态，从而当 daemon 重启的时候，容器不会因为管道（pipe）的关闭而终止。

			- 将容器的退出状态反馈给 daemon。

### 第6章　Docker 镜像

- https://pic1.imgdb.cn/item/6342695316f2c2beb153f72e.jpg
简介

	- Docker 镜像就像停止运行的容器，可以将镜像理解为类（Class），镜像可以理解为一种构建时（build-time）结构，而容器可以理解为一种运行时（run-time）结构

- 详解

	- 镜像和容器

		- 使用 docker container run 和 docker service create 命令从某个镜像启动一个或多个容器。

		- 一旦容器从镜像启动后，二者之间就变成了互相依赖的关系，并且在镜像上启动的容器全部停止之前，镜像是无法被删除的。

	- 镜像通常比较小

	- 拉取镜像

		- docker image ls

			- 检查 Docker 主机的本地仓库中是否包含镜像

			- 提供 --filter 参数来过滤镜像列表内容

				- dangling

					- 可以指定true 或者false ，仅返回悬虚镜像（true），或者非悬虚镜像（false）

					- 那些没有标签的镜像被称为悬虚镜像，在列表中展示为 <none>:<none> 

					- docker image ls --filter dangling=true

				- before

					- 需要镜像名称或者 ID 作为参数，返回在之前被创建的全部镜像

				- since

					- 与 before 类似，不过返回的是指定镜像之后创建的全部镜像

				- label

					- 根据标注（label）的名称或者值，对镜像进行过滤

				- reference

					- docker image ls --filter=reference="*:latest"

			- 使用 --format 参数来通过 Go 模板对输出内容进行格式化

				- docker image ls --format "{{.Size}}"

					- 返回 Docker 主机上镜像的大小属性

				- docker image ls --format "{{.Repository}}: {{.Tag}}: {{.Size}}"

					- 返回全部镜像，但是只显示仓库、标签和大小信息

		- 将镜像取到 Docker 主机本地的操作是拉取。

	- 镜像仓库服务

		- Docker 客户端的镜像仓库服务是可配置的，默认使用 Docker Hub

		- 镜像仓库服务包含多个镜像仓库（Image Repository）。同样，一个镜像仓库中可以包含多个镜像。

		- Docker Hub 也分为官方仓库（Official Repository）和非官方仓库（Unofficial Repository）

	- 镜像命名和标签

		- docker image pull <repository>:<tag>

			- 只需要给出镜像的名字和标签，就能在官方仓库中定位一个镜像

		- 如果没有在仓库名称后指定具体的镜像标签，则 Docker 会假设用户希望拉取标签为 latest 的镜像。

	- 为镜像打多个标签

		- 一个镜像可以根据用户需要设置多个标签

		- latest 是一个非强制标签，不保证指向仓库中最新的镜像

	- 通过 CLI 方式搜索 Docker Hub

		- docker search

			- 允许通过 CLI 的方式搜索 Docker Hub

			- 可以通过「NAME」字段的内容进行匹配，并且基于返回内容中任意列的值进行过滤。

			- 可以使用 --filter "is-official=true" ，使命令返回内容只显示官方镜像

			- 可以使用 --filter "is-automated=true"，使命令返回只显示自动创建的仓库

			- 默认情况下，Docker 只返回 25 行结果。可以指定 --limit 参数来增加返回内容行数，最多为 100 行。

	- https://pic1.imgdb.cn/item/6342747216f2c2beb1675947.jpg
镜像和分层

		- Docker 镜像由一些松耦合的只读镜像层组成。Docker 负责堆叠这些镜像层，并且将它们表示为单个统一的对象。

		- docker image pull 命令的输出，以 Pull complete 结尾的每一行都代表了镜像中某个被拉取的镜像层。

		- docker image inspect

			- 使用了镜像的 SHA256 散列值来标识镜像层。

		- docker history

			- 显示了镜像的构建历史记录，但其并不是严格意义上的镜像分层。

		- 所有的 Docker 镜像都起始于一个基础镜像层，当进行修改或增加新的内容时，就会在当前镜像层之上，创建新的镜像层。

	- 共享镜像层

		- 多个镜像之间可以并且确实会共享镜像层。这样可以有效节省空间并提升性能。

	- 根据摘要拉取镜像

		- 每一个镜像都有一个基于其内容的密码散列值，在 docker image ls 命 令之后添加 --digests 参数即可在本地查看镜像摘要

		- 已知镜像的摘要，那么可以使用摘要值再次拉取这个镜像

	- 多层架构的镜像

		- 某个镜像仓库标签（repository:tag）下的镜像可以同时支持 64 位 Linux、PowerPC Linux、64 位 Windows 和 ARM 等多种架构

		- Manifest列表（新）

			- 某个镜像标签支持的架构列表。其支持的每种架构，都有自己的 Mainfest 定义，其中列举了该镜像的构成。

	- 删除镜像

		- docker image rm

			- 在当前主机上删除该镜像以及相关的镜像层。

			- 如果某个镜像层被多个镜像共享，那只有当全部依赖该镜像层的镜像都被删除后，该镜像层才会被删除。

			- 如果被删除的镜像上存在运行状态的容器，那么删除操作不会被允许。

			- docker image rm $(docker image ls -q) -f

				- 删除 Docker 主机上全部镜像的快捷方式

- 命令

	- docker image pull

		- 下载镜像的命令

	- docker image ls

		- 列出了本地 Docker 主机上存储的镜像

	- docker image inspect

		- 完美展示了镜像的细节，包括镜像层数据和元数据。

	- docker image rm

		- 删除镜像

### 第7章　Docker 容器

- 简介

	- 容器是镜像的运行时实例

- 详解

	- 容器vs虚拟机

		- Hypervisor 是硬件虚拟化，将硬件物理资源划分为虚拟资源

		- 容器是操作系统虚拟化，容器将系统资源划分为虚拟资源。

		- 虚拟机的额外开销

			- 使用容器可以在更少的资源上运行更多的应用，启动更快，并且支付更少的授权和管理费用，同时面对未知攻击的风险也更小

	- 检查 Docker daemon

		- docker version

		- service docker status

		- systemctl is-active docker

	- 启动一个简单容器

		- docker container run

	- 容器进程

		- 容器如果不运行任何进程则无法存在

		- docker container ls

			- 观察当前系统正在运行的容器列表。

		- docker container exec

			- 将终端重新连接到 Docker

	- 容器生命周期

		- 可以根据需要多次停止、启动、暂停以及重启容器，并且这些操作执行得很快。

		- 容器及其数据是安全的。直至明确删除容器前，容器都不会丢弃其中的数据。

		- 如果将容器数据存储在卷中，数据也会被保存下来。

		- 优雅地停止容器

			- 先停止容器然后删除，给容器中运行的应用/进程一个停止运行并清理残留数据的机会

	- 利用重启策略进行容器的自我修复

		- 重启策略

			- always

				- 除非容器被明确停止，比如通过 docker container stop 命令，否则该策略会一直尝试重启处于停止状态的容器。

			- unless-stopped

				- 指定了 --restart unless-stopped 并处于 Stopped (Exited) 状态的容器，不会在 Docker daemon 重启的时候被重启。

			- on-failed

				- 退出容器并且返回值不是 0 的时候，重启容器。就算容器处于 stopped 状态，在 Docker daemon 重启的时候，容器也会被重启。

- 命令

	- docker container run

		- 启动新容器

	- docker container ls

		- 列出所有在运行（UP）状态的容器

		- 使用-a 标记，还可以看到处于停止（Exited）状态的容器。

	- docker container exec

		- 允许用户在运行状态的容器中，启动一个新进程。

	- docker container stop

		- 停止运行中的容器，并将状态置为Exited(0)

	- docker container start

		- 重启处于停止（Exited）状态的容器，可以指定容器的名称或者 ID

	- docker container rm

		- 删除停止运行的容器

		- 推荐首先使用 docker container stop 命令停止容器，然后使用 docker container rm 来完成删除

### 第8章　应用的容器化

- 简介

	- 将应用整合到容器中并且运行起来的这个过程，称为「容器化」，容器能够简化应用的构建、部署和运行过程

	- https://pic1.imgdb.cn/item/63428a8316f2c2beb18f20c6.jpg
应用容器化过程主要步骤

		- 编写应用代码

		- 创建一个 Dockerfile，其中包括当前应用的描述、依赖以及该如何运行这个应用

		- 对该 Dockerfile 执行 docker image build 命令。

		- 等待 Docker 将应用程序构建到 Docker 镜像中

- 详解

	- 单体应用容器化

		- Dockerfile 用途

			- 对当前应用的描述

			- 指导 Docker 完成应用的容器化（创建一个包含当前应用的镜像）

- 命令

	- docker image build

		- 读取 Dockerfile，并将应用程序容器化

		- 使用 -t 参数为镜像打标签

		- 使用 -f 参数指定 Dockerfile 的路径和名称

	- Dockerfile 中的 FROM 指令用于指定要构建的镜像的基础镜像。它通常是 Dockerfile 中的第一条指令。

	- Dockerfile 中的 RUN 指令用于在镜像中执行命令，这会创建新的镜像层。每个 RUN 指令创建一个新的镜像层。 

	- Dockerfile 中的 COPY 指令用于将文件作为一个新的层添加到镜像中。通常使用 COPY 指令将应用代码赋值到镜像中。

	- Dockerfile 中的 EXPOSE 指令用于记录应用所使用的网络端口。

	- Dockerfile 中的 ENTRYPOINT 指令用于指定镜像以容器方式启动后默认运行的程序。

### 第9章　使用 Docker Compose 部署应用

- 简介

	- Docker Compose 解决部署和管理繁多的服务问题，通过一个声明式的配置文件描述整个应用，从而使用一条命令完成部署。

- 详解

	- docker-compose.yml 文件结构

		- version

			- 必须指定，总是位于文件的第一行。定义了 Compose 文件格式（主要是API）的版本。

		- services

			- 定义不同的应用服务

			- Docker Compose 会将每个服务部署为一个容器，并且会使用 key 作为容器名字的一部分

			- 服务定义指令

				- build

					- 指定 Docker 基于当前目录下 Dockerfile 中定义的指令来构建一个新镜像。该镜像会被用于启动该服务的容器

				- command

					- 指定 Docker 在容器中执行名为 XXX 作为主程序

				- ports

					- 指定 Docker 将容器内（-target ）的 XXX 端口映射到主机（published ）的 XXX 端口

				- networks

					- 使得 Docker 可以将服务连接到指定的网络上

				- volumes

					- 卷挂载

		- networks

			- 指引 Docker 创建新的网络

		- volumes

			- 指引 Docker 来创建新的卷

- 命令

	- docker-compose up

		- 部署一个 Compose 应用

		- 默认情况下该命令会读取名为 docker-compose.yml 或 docker-compose.yaml 的文件，当然用户也可以使用 -f 指定其他文件名

		- 通常情况下，会使用 -d 参数令应用在后台启动。

	- docker-compose stop

		- 停止 Compose 应用相关的所有容器，但不会删除它们

		- 被停止的应用可以很容易地通过 docker-compose restart 命令重新启动。

	- docker-compose rm

		- 删除已停止的 Compose 应用。它会删除容器和网络，但是不会删除卷和镜像。

	- docker-compose restart

		- 重启已停止的 Compose 应用

		- 如果用户在停止该应用后对其进行了变更，那么变更的内容不会反映在重启后的应用中，这时需要重新部署应用使变更生效。

	- docker-compose ps

		- 列出 Compose 应用中的各个容器

		- 输出内容包括当前状态、容器运行的命令以及网络端口。

	- docker-compose down

		- 停止并删除运行中的 Compose 应用

		- 删除容器和网络，但是不会删除卷和镜像。

### 第10章　Docker Swarm

- 简介

	- Swarm 核心组件

		- 企业级的 Docker 安全集群

			- Swarm 将一个或多个 Docker 节点组织起来，使得用户能够以集群方式管理它们

		- 微服务应用编排引擎

			- Swarm 提供了一套丰富的 API 使得部署和管理复杂的微服务应用变得易如反掌。

- 详解

	- https://pic1.imgdb.cn/item/634397cf16f2c2beb1636f9b.jpg
Swarm 概括

- 命令

	- docker swarm init

		- 创建一个新的Swarm。

		- 执行该命令的节点会成为第一个管理节点，并且会切换到 Swarm 模式。

	- docker swarm join-token

		- 查询加入管理节点和工作节点到现有 Swarm 时所使用的命令和 Token。

		- 要获取新增管理节点的命令，请执行 docker swarm join-token manager 命令；

		- 要获取新增工作节点的命令，请执行 docker swarm join-token worker 命令。

	- docker node ls

		- 要获取新增工作节点的命令，请执行 docker swarm join-token worker 命令。

	- docker service create

		- 创建一个新服务

	- docker service ls

		- 列出 Swarm 中运行的服务，以及诸如服务状态、服务副本等基本信息。

	- docker service ps <service>

		- 给出更多关于某个服务副本的信息。

	- docker service inspect

		- 获取关于服务的详尽信息。附加 --pretty 参数可限制仅显示重要信息。

	- docker service scale

		- 对服务副本个数进行增减。

	- docker service update

		- 对运行中的服务的属性进行变更。

	- docker service logs

		- 查看服务的日志。

	- docker service rm

		- 从 Swarm 中删除某服务。

### 第11章　Docker 网络

- 简介

	- 容器网络模型（CNM）

		- 单机桥接网络（Single-Host Bridge Network）

		- 多机覆盖网络（Multi-Host Overlay）

- 详解

	- 基础理论

		- Docker 网络架构主要部分构成

			- CNM

				- 设计标准，在 CNM 中，规定了 Docker 网络架构的基础组成要素。

				- https://pic1.imgdb.cn/item/6343b6d816f2c2beb1a571a9.jpg
基本要素

					- 沙盒（Sandbox）

						- 一个独立的网络栈

						- 包括以太网接口、端口、路由表以及 DNS 配置。

					- 终端（Endpoint）

						- 虚拟网络接口

						- 负责创建连接。在 CNM 中，终端负责将沙盒连接到网络。

					- 网络（Network）

						- 802.1d 网桥（类似交换机）的软件实现，网络就是需要交互的终端的集合，并且终端之间相互独立。

			- Libnetwork

				- CNM 的具体实现，并且被 Docker 采用。

				- 通过 Go 语言编写，并实现了 CNM 中列举的核心组件。

				- 此外它还实现了本地服务发现（Service Discovery）、基于 Ingress 的容器负载均衡，以及网络控制层和管理层功能。

			- https://pic1.imgdb.cn/item/6343b83116f2c2beb1a80e41.jpg
驱动

				- 通过实现特定网络拓扑的方式来拓展该模型的能力。

	- 单机桥接网络

		- 最简单的 Docker 网络

			- 单机

				- 意味着该网络只能在单个 Docker 主机上运行，并且只能与所在 Docker 主机上的容器进行连接。

			- 桥接

				- 802.1.d 桥接的一种实现（二层交换机）

	- 多机覆盖网络

		- 允许单个网络包含多个主机，这样不同主机上的容器间就可以在链路层实现通信。

- 命令

	- docker network ls

		- 列出运行在本地 Docker 主机上的全部网络。

	- docker network create

		- 创建新的 Docker 网络

		- docker network create -d overlay overnet 会创建一个新的名为 overnet 的覆盖网络，其采用的驱动为 Docker Overlay 。

	- docker network inspect

		- 提供 Docker 网络的详细配置信息

	- docker network prune

		- 删除 Docker 主机上全部未使用的网络

	- docker network rm

		- 删除 Docker 主机上指定网络

### 第12章　Docker 覆盖网络

- 简介

	- 创建扁平的、安全的二层网络来连接多个主机，容器可以连接到覆盖网络并直接互相通信。

	- Docker 提供了原生覆盖网络的支持，易于配置且非常安全。

- 详解

	- https://pic1.imgdb.cn/item/6345227816f2c2beb120e52b.jpg
VXLAN

		- 是一种封装技术，能使现存的路由器和网络架构看起来就像普通的IP/UDP包一样，并且处理起来毫无问题。

- 命令

	- docker network create

		- 创建新网络所使用的命令

		- -d 参数允许用户指定所用驱动，常见的驱动是Overlay 

	- docker network ls

		- 列出 Docker 主机上全部可见的容器网络

	- docker network inspect

		- 查看特定容器网络的详情，包括范围、驱动、IPv6、子网配置、VXLAN 网络 ID 以及加密状态

	- docker network rm

		- 删除指定网络

### 第13章　卷与持久化数据

- 简介

	- 数据分类

		- 持久化

			- 卷与容器是解耦的，可以独立地创建并管理卷，卷并未与任意容器生命周期绑定

			- 用户可以删除一个关联了卷的容器，但是卷并不会被删除

		- 非持久化

			- 每个 Docker 容器都有自己的非持久化存储

			- 非持久化存储自动创建，从属于容器，生命周期与容器相同

			- 删除容器也会删除全部非持久化数据

- 详解

	- 容器与非持久数据

		- 容器擅长无状态和非持久化事务

		- 非持久存储属于容器的一部分，并且与容器的生命周期一致

		- 容器创建时会创建非持久化存储，同时该存储也会随容器的删除而删除

	- 容器与持久化数据

		- 用户创建卷，然后创建容器，接着将卷挂载到容器上

		- 卷会挂载到容器文件系统的某个目录之下，任何写到该目录下的内容都会写到卷中。

		- 即使容器被删除，卷与其上面的数据仍然存在。

		- 创建和管理容器卷

			- 卷插件

				- 块存储

					- 相对性能更高，适用于对小块数据的随机访问负载。

				- 文件存储

					- 包括NFS和SMB协议的系统，同样在高性能场景下表现优异。

				- 对象存储

					- 适用于较大且长期存储的、很少变更的二进制数据存储。通常对象存储是根据内容寻址，并且性能较低。

		- 卷在容器和服务中的使用

			- 如果指定了已经存在的卷，Docker 会使用该卷

			- 如果指定的卷不存在，Docker 会创建一个卷

- 命令

	- docker volume create

		- 创建新卷。

		- 默认情况下，新卷创建使用 local 驱动，但是可以通过 -d 参数来指定不同的驱动。

	- docker volume ls

		- 列出本地 Docker 主机上的全部卷

	- docker volume inspect

		- 查看卷的详细信息

		- 查看卷在 Docker 主机文件系统中的具体位置

	- docker volume prune

		- 删除未被容器或者服务副本使用的全部卷。

	- docker volume rm

		- 删除未被使用的指定卷

### 第14章　使用 Docker Stack 部署应用

- 简介

	- Stack 提供了简单的方式来部署应用并管理其完整的生命周期：初始化部署 > 健康检查 > 扩容 > 更新 > 回滚，以及其他功能

- https://pic1.imgdb.cn/item/6346339716f2c2beb1e72426.jpg
详解

	- Stack 文件

		- Stack 文件就是 Docker Compose 文件。唯一的要求就是 version： 需要是「3.0」或者更高的值。

		- 网络

		- 密钥

		- 服务

- 命令

	- docker stack deploy

		- 根据 Stack 文件（通常是 docker-stack.yml ）部署和更新 Stack 服务的命令

	- docker stack ls

		- 列出 Swarm 集群中的全部 Stack，包括每个 Stack 拥有多少服务

	- docker stack ps

		- 列出某个已经部署的 Stack 相关详情，服务副本在节点的分布情况，以及期望状态和当前状态。

	- docker stack rm

		- 从 Swarm 集群中移除 Stack

### 第15章　Docker 安全

- 简介

	- Linux Docker 利用了大部分 Linux 通用的安全技术

		- 命名空间（Namespace）

		- 控制组（CGroup）

		- 系统权限（Capability）

		- 强制访问控制（MAC）系统

		- 安全计算（Seccomp）

	- Docker 平台原生安全技术

		- Docker Swarm模式

			- 默认是开启安全功能的

			- 无须任何配置，就可以获得加密节点 ID、双向认证、自动化 CA 配置、自动证书更新、加密集群存储、加密网络等安全功能。

		- Docker内容信任（Docker Content Trust, DCT）

			- 允许用户对镜像签名，并且对拉取的镜像的完整度和发布者进行验证。

		- Docker安全扫描（Docker Security Scanning）

			- 分析 Docker 镜像，检查已知缺陷，并提供对应的详细报告。

		- Docker 密钥

			- 存储在加密集群存储中，在容器传输过程中实时解密，使用时保存在内存文件系统并运行了一个最小权限模型。

- 详解

	- Linux 安全技术

		- Namespace

			- 将操作系统（OS）进行拆分，使一个操作系统看起来像多个互相独立的操作系统一样。

			- Docker 容器是由各种命名空间组合而成的

			- Docker 容器本质就是命名空间的有组织集合

			- Docker 如何使用每个命名空间

				- 进程 ID 命名空间

					- Docker 使用 PID 命名空间为每个容器提供互相独立的容器树

					- 每个容器都拥有自己的进程树，意味着每个容器都有自己的 PID 为 1 的进程

					- PID 命名空间也意味着容器不能看到其他容器的进程树，或者其所在主机的进程树。

				- 网络命名空间

					- Docker 使用 NET 命名空间为每个容器提供互相隔离的网络栈。

					- 网络栈中包括接口、ID 地址、端口地址以及路由表。

					- 每个容器都有自己的 eth0 网络接口，并且有自己独立的 IP 和端口地址。

				- 挂载点命名空间

					- 每个容器都有互相隔离的根目录 / 

					- 个容器都有自己的 /etc 、/var 、/dev 等目录

					- 容器内的进程不能访问 Linux 主机上的目录，或者其他容器的目录，只能访问自己容器的独立挂载命名空间。

				- 进程内通信命名空间

					- Docker 使用 IPC 命名空间在容器内提供共享内存。

					- IPC 提供的共享内存在不同容器间也是互相独立的。

				- 用户命名空间

					- Docker 允许用户使用 USER 命名空间将容器内用户映射到 Linux 主机不同的用户上。

					- 容器内的 root 用户映射到 Linux 主机的非 root 用户上。

				- UTS 命名空间

					- Docker 使用 UTS 命名空间为每个容器提供自己的主机名称

		- Control Group

			- 控制组用于限额

			- 容器之间是互相隔离的，但却共享 OS 资源，比如 CPU、RAM 以及硬盘 I/O。

			- CGroup 允许用户设置限制，这样单个容器就不能占用主机全部的 CPU、RAM 或者存储 I/O 资源了。

		- Capability

			- Linux root 用户由许多能力组成

				- CAP_CHOWN

					- 允许用户修改文件所有权。

				- CAP_NET_BIND_SERVICE

					- 允许用户将 socket 绑定到系统端口号。

				- CAP_SETUID

					- 允许用户提升进程优先级。

				- CAP_SYS_BOOT

					- 允许用户重启系统。

			- Docker 采用 Capability 机制来实现用户在以 root 身份运行容器的同时，还能移除非必须的 root 能力。

		- MAC

			- Docker 采用主流 Linux MAC 技术，允许用户在启动容器的时候不设置相应策略，还允许用户根据需求自己配置合适的策略。

		- Seccomp

			- Docker 使用过滤模式下的 Seccomp 来限制容器对宿主机内核发起的系统调用。

	- Docker平台安全技术

		- Swarm 模式

			- 加密节点 ID

			- 基于 TLS 的认证机制

			- 安全准入令牌

			- 支持周期性证书自动更新的 CA 配置

			- 加密集群存储（配置 DB）

			- 加密网络

			- Swarm 安全原理

				- Swarm 准入令牌

					- 向某个现存的 Swarm 中加入管理者和工作者所需的唯一凭证就是准入令牌

						- 管理者所需准入令牌

						- 工作者所需准入令牌

				- TLS 和双向认证

					- 每个加入 Swarm 的管理者和工作者节点，都需要发布自己的客户端证书

					- 证书用于双向认证，定义了节点相关信息，包括从属的 Swarm 集群以及该节点在集群中的身份（管理者还是工作者）。

				- 配置一些 CA 信息

					- Swarm 允许节点在证书过期前重新创建证书，这样可以保证 Swarm 中全部节点不会在同一时间尝试更新自己的证书信息。

				- 集群存储

					- 集群存储是 Swarm 的大脑，保存了集群配置和状态数据。

		- Docker 安全扫描

			- 一种深入检测 Docker 镜像是否存在已知安全缺陷的好方式。

		- Docker 内容信任

			- 帮助用户检查从 Docker 服务中拉取的镜像

		- Docker 密钥

### 第16章　企业版工具

- 简介

	- Docker EE 是企业版的 Docker。其内部包括了上百个引擎、操作界面以及私有安全注册。用户可以本地化部署，并且其中包括了一份支持协议。

- 详解

	- Docker EE 引擎

		- 提供 Docker 全部核心功能

			- 镜像

			- 容器管理

			- 网络

			- 卷

			- 集群

			- 安全

	- Docker 统一控制平台（UCP）

		- 企业级的容器即服务平台的图形化操作界面，基于 Swarm 模式下的 Docker EE 构建的

	- Docker 可信镜像仓库服务（DTR）

		- 私有的 Docker Hub，可以在本地部署，并且自行管理。

### 第17章　企业级特性

- 简介

	- 企业版 Docker 是一个强化版本，包含 Docker 引擎、运维界面、安全镜像库以及一系列面向企业的特性。它可以被部署在私有云或公有云上，用户可自行管理，并会获取一份支持协议。

- 详解

	- 基于角色的权限控制（RBAC）

		- 主体（Subject）

			- 一个或多个用户，或一个团队。

		- 角色（Role）

			- 角色是一系列权限的组合

		- 集合（Collection）

			- 权限作用的资源

	- 集成活动目录

		- 能够与活动目录及其他 LDAP 目录服务进行集成，从而利用组织中现有的单点登录系统中的用户和组

	- Docker 内容信任机制（DCT）

		- Docker 镜像的发布者可以在将镜像推送到库中时对其进行签名。使用者可以在拉取镜像时进行校验，或进行构建或运行等操作。

		- DCT 确保使用者能够得到他们想要的镜像

	- 配置 Docker 可信镜像仓库服务（DTR）

	- 使用 Docker 可信镜像仓库服务

		- Docker 可信镜像服务是一种安全的、自行配置和管理的私有镜像库

	- 镜像提升

		- 利用镜像提升功能可以构建一条基于一定策略的自动化流水线，它能够通过同一个 DTR 中的多个镜像库实现镜像提升。

	- HTTP 路由网格（HRM）

		- 利用镜像提升功能可以构建一条基于一定策略的自动化流水线，它能够通过同一个 DTR 中的多个镜像库实现镜像提升。

