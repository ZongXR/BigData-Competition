#!/bin/bash
# 从节点启动zk
/usr/zookeeper/zookeeper-3.4.10/bin/zkServer.sh start
# 等待主节点启动hadoop
read -p "等待主节点启动hadoop完成再继续"
# 从节点启动hive
nohup /usr/hive/apache-hive-2.1.1-bin/bin/hive --service metastore &
