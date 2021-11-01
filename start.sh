#!/bin/bash
# 记录从节点
index=1
for arg in "$@"
do
  index=$((index+1))
done
# 主节点启动zk
zkServer.sh start
zkServer.sh status
jps
# 从节点启动zk
for ((i=1; i<index; i++))
do
  ssh root@slave${i} "zkServer.sh start"
  ssh root@slave${i} "zkServer.sh status"
done
# 主节点启动hadoop
start-all.sh
hdfs dfsadmin -report
for ((i=1; i<index; i++))
do
  ssh root@slave${i} "jps"
done
