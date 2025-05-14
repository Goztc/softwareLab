## git

### 查看远程分支和本地分支
```bash
git branch -a
# 输出
* main
  remotes/origin/THM_file_upload
  remotes/origin/main
```

### 下载远程分支到本地并切换
```
git checkout -b THM_file_upload origin/THM_file_upload
```

### 合并分支
```
git checkout main
git fetch origin
git merge THM_file_upload
```

### 查看合并带来的修改
```bash
# 如果合并已经提交
git log -1 -p
# 如果合并后没提交
git diff
```