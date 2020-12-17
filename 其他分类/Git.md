# Git

## Git 的工作流程及区域划分

### Workspace

- 工作区

### Staging/Index

- 暂存区

### Local Repository

- 本地仓库（可修改）

### /refs/remotes

- 远程仓库的引用（不可修改）

### Remote

- 远程仓库

## Git 常用命令集合

### 简单命令

- git init

	- 在当前目录新建一个 git 仓库

- gitk

	- 打开 git 仓库图形界面

- git status

	- 显示所有变更信息

- git clean -fd

	- 删除所有 Untracked files

- git fetch remote

	- 下载远程仓库的所有更新

- git pull romote branch-name

	- 下载远程仓库的所有更新，并且 Merge（合并）

- git rev-parse HEAD 

	- 查看上次 commit id

- git merge branch-name

	- 将指定分支合并到当前分支

- git format-patch HEAD^ 

	- 将最近的一次 commit 打包到 patch 文件中

- git am  patch-file

	- 将 patch 文件 添加到本地仓库

- git blame file-name

	- 看指定文件修改历史

### 常用命令 

- git clone

	- git clone url

		- 将远程 git 仓库克隆到本地

	- git clone -b branch url 

		- 将远程 git 仓库对应分支克隆到本地

- git stash

	- git stash

		- 将修改过，未 add 到 Staging 暂存区的文件，暂时存储起来

	- git stash apply

		- 恢复之前 stash 存储的内容

	- git stash save "stash test"

		- 保存 stash 并写 message

	- git stash list

		- 查看 stash 了哪些存储

	- git stash apply stash@{1}

		- 将 stash@{1} 存储的内容还原到工作区

	- git stash drop stash@{1}

		- 删除 stash@{1} 存储的内容

	- git stash clear

		- 除所有缓存的 stash

- git config

	- git config --global gui.encoding=utf-8 

		- 配置 git 图形界面编码为 utf-8

	- git config --global user.name name  

		- 置全局提交代码的用户名 

	- git config --global user.email email

		- 置全局提交代码时的邮箱

	- git config user.name name  

		- 设置当前项目提交代码的用户名 

- git remote

	- git remote -v  

		- 显示所有远程仓库

	- git remote add name url 

		- 增加一个新的远程仓库

	- git remote remove name

		- 删除指定远程仓库

	- git remote show origin

		- 获取指定远程仓库的详细信息

- git add

	- git add .
git add --all  

		- 添加所有的修改到 Staging 暂存区

	- git add file   

		- 添加指定文件到 Staging 暂存区

	- git add file1 file2   

		- 添加多个修改的文件到 Staging 暂存区

	- git add dir

		- 添加修改的目录到 Staging 暂存区

	- git add src/main*  

		- 添加所有 src 目录下 main 开头的所有文件到 Staging 暂存区    

- git commit

	- git commit -m "message"  

		- 提交 Staging 暂存区的代码到本地仓库区

	- git commit file1 file2 -m "message"  

		- 提交 Staging 暂存区中在指定文件到本地仓库区

	- git commit --amend -m "message" 

		- 使用新的一次 commit，来覆盖上一次 commit

	- git commit --amend --author="name <email>" --no-edit

		- 修改上次提交的用户名和邮箱

- git branch

	- git branch   

		- 列出本地所有分支

	- git branch -v

		- 列出本地所有分支 并显示最后一次提交的哈希值

	- git branch -vv

		- 在-v 的基础上显示上游分支的名字

	- git branch -r  

		- 出上游所有分支

	- git branch branch-name  

		- 新建一个分支，但依然停留在当前分支

	- git branch -d branch-name   

		- 删除分支

	- git branch --set-upstream-to origin/master

		- 设置分支上游

	- git branch -m old-branch new-branch

		- 本地分支重命名

- git checkout

	- git checkout -b local-branch origin/remote-branch

		- 创建本地分支并关联远程分支

	- git checkout -b branch-name

		- 新建一个分支，且切换到新分支

	- git checkout branch-name  

		- 切换到另一个分支

	- git checkout commit-file  

		- 撤销工作区文件的修改，跟上次 Commit 一样

- git tag

	- git tag -a v1.4 -m 'my version 1.4'

		- 创建带有说明的标签

	- git tag tag-name

		- 打标签

	- git tag 

		- 查看所有标签

	- git tag tag-name commit-id

		- 给指定 commit 打标签

	- git tag -d tag-name   

		- 删除标签

- git push

	- git push origin master

		- 将本地的 master 分支推送到 origin 主机的master 分支。如果 master 不存在，则会被新建。

	- git push origin :master   
git push origin --delete master

		- 删除远程分支，注意和上面的区别

	- git push origin --delete tag tag-name

		- 删除远程标签

	- git push remote branch-name

		- 上传本地仓库到远程分支

	- git push remote branch-name --force

		- 强行推送当前分支到远程分支

	- git push remote --all  

		- 推送所有分支到远程仓库

	- git push --tags

		- 推送所有标签

	- git push origin tag-name

		- 推送指定标签

	- git push origin :refs/tags/tag-name  

		- 删除远程标签（需要先删除本地标签）

	- git push origin dev:master

		- 将本地 dev 分支 push 到远程 master 分支

- git reset

	- git reset HEAD

		- 将未 commit 的文件移出 Staging 暂存区

	- git reset --hard  

		- 重置 Staging 暂存区与上次 commit 的一样

	- git reset --hard origin/master

		- 重置 Commit 代码和远程分支代码一样

	- git reset --hard HEAD^

		- 回退到上个 commit

	- git reset --hard HEAD~3

		- 回退到前 3 次提交之前，以此类推，回退到 n 次提交之前

	- git reset --hard commit-id     

		- 回退到指定 commit

- git diff

	- git diff file-name

		- 查看文件在工作区和暂存区区别

	- git diff --cached  file-name

		- 查看暂存区和本地仓库区别

	- git diff branch-name file-name

		- 查看文件和另一个分支的区别

	- git diff commit-id commit-id  

		- 查看两次提交的区别

- git show

	- git show tag-name

		- 查看指定标签的提交信息

	- git show commit-id 

		- 查看具体的某次改动

- git log

	- git log --pretty=format:"%h %cn %s %cd" --author="Glorze\|高老四"  --date=short src

		- 指定文件夹 log

	- git log --pretty=format:"%h %cn %s %cd" --author=Glorze--date=short 

		- 查看指定用户指定 format  提交

	- git log --pretty=oneline file

		- 查看该文件的改动历史

	- git log --graph --pretty=oneline --abbrev-commit

		- 图形化查看历史提交

	- git log --pretty='%aN' | sort | uniq -c | sort -k1 -n -r | head -n 5

		- 统计仓库提交排名前 5

	- git log --author="Glorze" --pretty=tformat: --numstat | awk '{ add += $1 ; subs += $2 } END { printf "added lines: %s removed lines : %s \n",add,subs }'

		- 查看指定用户添加代码行数，和删除代码行数

- git rebase

	- git rebase branch-name

		- 将指定分支合并到当前分支

	- git rebase -i commit-id

		- 执行 commit id  将 rebase 停留在指定 commit 处

	- git rebase -i --root

		- 执行 commit id 将 rebase 停留项目首次 commit 处

- git restore

	- git restore --staged file

		- 恢复第一次 add 的文件，同 git rm --cached

	- git restore file

		- 移除 staging 区的文件，同 git checkout

- git revert

	- git revert HEAD

		- 撤销前一次 commit

	- git revert HEAD^

		- 撤销前前一次 commit

	- git revert commit-id

		- 撤销指定某次 commit

## Git 常见问题处理方式

### 代码未完成并且要切换到其他分支

- git stash 当前分支

	- 去其他分支处理事情
	- 回到当前分支还原暂存的代码 git stash apply
	- git stash save "修改的信息"
	- git stash pop
	- git stash list
	- git stash apply stash@{0}

- 及时 commit 代码，不 push

### 合并其他分支的指定 Commit

- git cherry-pick 指定commit-id

## git merge 的三种场景

### 快进式合并（fast-forward）

- 如果顺着一个分支走下去能够到达另一个分支，那么Git在合并两者的时候， 只会简单的将指针向前推进（指针右移），因为这种情况下的合并操作没有需要解决的分歧

### 三方合并

- 做了一个新的快照并且自动创建一个新的提交指向它。这个被称作一次合并提交，它的特别之处在于他有不止一个父提交。

### 遇到冲突时的合并

- 如果在两个分支分别对同一个文件做了改动，Git 就没法直接合并他们。当遇到冲突的时候，Git 会自动停下来，等待我们解决冲突。

