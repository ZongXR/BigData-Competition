#!/bin/bash
# 下载资源
yum install -y net-tools
yum install -y vim
yum install -y ntp
yum install -y wget
wget https://repo.huaweicloud.com/java/jdk/8u171-b11/jdk-8u171-linux-x64.tar.gz
wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.10/zookeeper-3.4.10.tar.gz
wget https://archive.apache.org/dist/hadoop/core/hadoop-2.7.3/hadoop-2.7.3.tar.gz
wget https://repo.mysql.com//mysql57-community-release-el7-11.noarch.rpm

# 基础环境配置
# 配置主机名
hostnamectl set-hostname master
# 关掉防火墙
systemctl stop firewalld
systemctl disable firewalld
# 配置hosts
local_ip=`ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|awk '{print $2}'|tr -d "addr:"​`
echo -e "\n${local_ip} master" >> /etc/hosts
index=1
for arg in "$@"
do
  echo -e "$arg slave${index}" >> /etc/hosts
  index=$((index+1))
done
# 配置免密登录
ssh-keygen
ssh-copy-id master
for ((i=1; i<index; i++))
do
  ssh-copy-id slave${i}
done
# 复制hosts文件
for ((i=1; i<index; i++))
do
  scp /etc/hosts root@slave${i}:/etc/
done
# 配置时区
tzselect
# 设置时钟
sed -i '25 i fudge 127.127.1.0 stratum 10' /etc/ntp.conf
sed -i '25 i server 127.127.1.0' /etc/ntp.conf
sed -i 's/^[^#].*centos.pool.ntp.org*/#&/g'  /etc/ntp.conf
systemctl start ntpd
systemctl enable ntpd
# 同步时钟
for ((i=1; i<index; i++))
do
  ssh root@slave${i} "ntpdate master"
done
# 配置java
mkdir -p /usr/java
tar -zxvf ./jdk-8u171-linux-x64.tar.gz -C /usr/java
# 配置zookeeper
mkdir -p /usr/zookeeper
tar -zxvf ./zookeeper-3.4.10.tar.gz -C /usr/zookeeper
mv /usr/zookeeper/zookeeper-3.4.10/conf/zoo_sample.cfg /usr/zookeeper/zookeeper-3.4.10/conf/zoo.cfg
sed -i 's/^dataDir=*/#&/g' /usr/zookeeper/zookeeper-3.4.10/conf/zoo.cfg
mkdir -p /usr/zookeeper/zookeeper-3.4.10/zkdata
mkdir -p /usr/zookeeper/zookeeper-3.4.10/zkdatalog
echo 'dataDir=/usr/zookeeper/zookeeper-3.4.10/zkdata' >> /usr/zookeeper/zookeeper-3.4.10/conf/zoo.cfg
echo 'dataLogDir=/usr/zookeeper/zookeeper-3.4.10/zkdatalog' >> /usr/zookeeper/zookeeper-3.4.10/conf/zoo.cfg
echo 'server.1=master:2888:3888' >> /usr/zookeeper/zookeeper-3.4.10/conf/zoo.cfg
for ((i=1; i<index; i++))
do
  echo "server.$((i+1))=slave$i:2888:3888" >> /usr/zookeeper/zookeeper-3.4.10/conf/zoo.cfg
done
echo '1' > /usr/zookeeper/zookeeper-3.4.10/zkdata/myid
for ((i=1; i<index; i++))
do
  ssh root@slave${i} "mkdir -p /usr/zookeeper/zookeeper-3.4.10/zkdata"
  ssh root@slave${i} "mkdir -p /usr/zookeeper/zookeeper-3.4.10/zkdatalog"
  ssh root@slave${i} "echo $((i+1)) > /usr/zookeeper/zookeeper-3.4.10/zkdata/myid"
  scp /usr/zookeeper/zookeeper-3.4.10/conf/zoo.cfg root@slave${i}:/usr/zookeeper/zookeeper-3.4.10/conf
done
# 配置hadoop
mkdir -p /usr/hadoop
tar -zxvf ./hadoop-2.7.3.tar.gz -C /usr/hadoop
sed -i '25 i export JAVA_HOME=/usr/java/jdk1.8.0_171' /usr/hadoop/hadoop-2.7.3/etc/hadoop/hadoop-env.sh
sed -i '18 i export JAVA_HOME=/usr/java/jdk1.8.0_171' /usr/hadoop/hadoop-2.7.3/etc/hadoop/yarn-env.sh
touch /usr/hadoop/hadoop-2.7.3/etc/hadoop/excludes
echo "master" > /usr/hadoop/hadoop-2.7.3/etc/hadoop/master
echo "" > /usr/hadoop/hadoop-2.7.3/etc/hadoop/slaves
for ((i=1; i<index; i++))
do
  echo "slave$i" >> /usr/hadoop/hadoop-2.7.3/etc/hadoop/slaves
done
cp -r ./hadoop /usr/hadoop/hadoop-2.7.3/etc
for ((i=1; i<index; i++))
do
  scp -r /usr/hadoop/hadoop-2.7.3/etc/hadoop root@slave${i}:/usr/hadoop/hadoop-2.7.3/etc
done
# 配置环境变量
echo '# timezone' >> /etc/profile
echo "TZ='Asia/Shanghai'; export TZ" >> /etc/profile
echo '# java' >> /etc/profile
echo 'export JAVA_HOME=/usr/java/jdk1.8.0_171' >> /etc/profile
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> /etc/profile
echo 'export CLASSPATH=$JAVA_HOME/lib' >> /etc/profile
echo 'export JAVA_HOME PATH CLASSPATH' >> /etc/profile
echo '# zookeeper' >> /etc/profile
echo 'export ZOOKEEPER_HOME=/usr/zookeeper/zookeeper-3.4.10' >> /etc/profile
echo 'export PATH=$PATH:$ZOOKEEPER_HOME/bin' >> /etc/profile
echo '# hadoop' >> /etc/profile
echo 'export HADOOP_HOME=/usr/hadoop/hadoop-2.7.3' >> /etc/profile
echo 'export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin' >> /etc/profile
echo 'unset MAILCHECK' >> /etc/profile
source /etc/profile
for ((i=1; i<index; i++))
do
  scp /etc/profile root@slave${i}:/etc/
  ssh root@slave${i} "source /etc/profile"
done
# 格式化节点
hadoop namenode -format



