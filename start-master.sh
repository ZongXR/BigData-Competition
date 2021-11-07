#!/bin/bash
# 主节点启动zk
/usr/zookeeper/zookeeper-3.4.10/bin/zkServer.sh start
# 主节点启动hadoop
/usr/hadoop/hadoop-2.7.3/sbin/start-all.sh
/usr/hadoop/hadoop-2.7.3/bin/hdfs dfsadmin -report
/usr/hadoop/hadoop-2.7.3/bin/hdfs dfsadmin -refreshNodes
/usr/hadoop/hadoop-2.7.3/sbin/start-balancer.sh
# 启动spark
/usr/spark/spark-2.4.3-bin-hadoop2.7/sbin/start-all.sh
