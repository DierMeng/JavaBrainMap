# CentOS

## 目录简介

### bin（binary）

- 放置一些系统必备的执行档，普通用户和 root 用户都可以执行

	- cat 连接文件或标准输入并打印，常用来显示文件内容
	- cp 复制文件或者目录
	- chmod 改变 linux 系统文件或目录的访问权限
	- df 显示指定磁盘文件的可用空间
	- dmesg 检查和控制内核的环形缓冲区
	- gzip 对文件进行压缩和解压缩
	- kill 终止指定的进程的运行
	- ls（list）打印出当前目录的清单
	- mkdir 创建指定的名称的目录
	- more 类似 cat ，cat 命令是整个文件的内容从上到下显示在屏幕上。 more 会以一页一页的显示方便使用者逐页阅读
	- mount 将分区挂接到 Linux 的一个文件夹下，从而将分区和该目录联系起来
	- rm 删除一个目录中的一个或多个文件或目录
	- su 切换当前用户身份到其他用户身份
	- tar 为 linux 的文件和目录创建档案

### sbin

- 放置一些系统管理的必备程式，只有root用户可以执行

	- cfdisk
	- dhcpcd
	- dump
	- e2fsck
	- fdisk
	- halt
	- ifconfig
	- ifup
	- ifdown
	- init
	- insmod
	- lilo
	- lsmod
	- mke2fs
	- modprobe
	- quotacheck
	- reboot
	- rmmod
	- runlevel
	- shutdown

### boot

- 启动目录，存的是启动相关的文件，该目录下不要乱存东西

### dev

- 设备文件保存目录

### etc

- 配置文件保存目录

### home

- 普通用户的家目录

### lib

- 系统库保存目录

### lib64

- X86_64 的 Linux 系统， 就会有 /usr/lib64/ 目录产生 

### media

- 挂载目录

### mnt

- 暂时挂载某些额外的装置

### opt

- 第三方协力软体放置的目录

### proc

- 直接写入内存的，虚拟文件系统

### root

- 超级用户的家目录

### run

- 某些程序或者是服务启动后，会将他们的PID放置在这个目录下

### srv（service）一些网路服务启动之后，这些服务所需要取用的资料目录

### sys

- 直接写入内存的，虚拟文件系统

### tmp

- 临时目录

### usr

- Unix 操作系统软件资源所放置的目录

	- /usr/bin

		- 放置一些应用软体工具的必备执行档

			- c++、g++、gcc、chdrv
			- diff、dig、du、eject
			- elm、free、gnome*、 gzip
			- htpasswd、kfm、ktop、last
			- less、locale、m4、make
			- man、mcopy、ncftp、 newaliases
			- nslookup passwd、quota、smb*、wget

	- /usr/sbin

		- 放置一些网路管理的必备程式

			- dhcpd、httpd、imap、in.*d
			- inetd、lpd、named、netconfig
			- nmbd、samba、sendmail、squid
			- swap、tcpd、tcpdump

### var

- 系统相关文档内容

