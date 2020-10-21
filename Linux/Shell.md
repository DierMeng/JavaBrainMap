# Shell

## 命令杂记

### 通过 Nginx access 日志实时统计单台机器 QPS

- tail -f access.log | awk -F '[' '{print $2}' | awk 'BEGIN{key="";count=0}{if(key==$1){count++}else{printf("%s\t%d\r\n", key, count);count=1;key=$1}}'
- tail -f access.log | awk -F '[' '{print $2}' | awk '{print $1}' | uniq -c
- cat access.log | awk -F '[' '{print $2}' | awk '{print $1}' | sort | uniq -c |sort -k1,1nr

