#!/bin/bash
# 下载资源
yum install -y net-tools
yum install -y vim
yum install -y ntp
yum install -y wget
yum install -y unzip zip
wget https://repo.huaweicloud.com/java/jdk/8u171-b11/jdk-8u171-linux-x64.tar.gz
wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.10/zookeeper-3.4.10.tar.gz
wget https://archive.apache.org/dist/hadoop/core/hadoop-2.7.3/hadoop-2.7.3.tar.gz
wget https://repo.mysql.com/mysql57-community-release-el7-11.noarch.rpm
wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.47.zip
wget https://archive.apache.org/dist/hive/hive-2.1.1/apache-hive-2.1.1-bin.tar.gz
wget https://scala-lang.org/files/archive/scala-2.11.11.tgz
wget https://archive.apache.org/dist/spark/spark-2.4.3/spark-2.4.3-bin-hadoop2.7.tgz

# 基础环境配置
# 配置主机名
hostnamectl set-hostname master
# 关掉防火墙
systemctl stop firewalld
systemctl disable firewalld
# 配置hosts
local_ip=$(ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|awk '{print $2}'|tr -d "addr:")
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
# 设置从节点主机名
for ((i=1; i<index; i++))
do
  ssh root@slave${i} "hostnamectl set-hostname slave$i"
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
# 安装hive
mkdir -p /usr/hive
tar -zxvf ./apache-hive-2.1.1-bin.tar.gz -C /usr/hive
echo 'export HADOOP_HOME=/usr/hadoop/hadoop-2.7.3' >> /usr/hive/apache-hive-2.1.1-bin/conf/hive-env.sh
echo 'export HIVE_CONF_DIR=/usr/hive/apache-hive-2.1.1-bin/conf' >> /usr/hive/apache-hive-2.1.1-bin/conf/hive-env.sh
echo 'export HIVE_AUX_JARS_PATH=/usr/hive/apache-hive-2.1.1-bin/lib' >> /usr/hive/apache-hive-2.1.1-bin/conf/hive-env.sh
cp /usr/hive/apache-hive-2.1.1-bin/lib/jline-2.12.jar /usr/hadoop/hadoop-2.7.3/share/hadoop/yarn/lib/
unzip -o -d ./ mysql-connector-java-5.1.47.zip
cp ./mysql-connector-java-5.1.47/mysql-connector-java-5.1.47-bin.jar /usr/hive/apache-hive-2.1.1-bin/lib
cp ./hive/hive-master.xml /usr/hive/apache-hive-2.1.1-bin/conf/hive-site.xml
for ((i=1; i<index; i++))
do
  sed -e "s/jdbc:mysql:\/\/slave/jdbc:mysql:\/\/slave$i/" ./hive/hive-slave.xml > ./hive/hive-slave$i.xml
  scp ./hive/hive-slave$i.xml root@slave$i:/usr/hive/apache-hive-2.1.1-bin/conf/hive-site.xml
  ssh root@slave${i} "/usr/hive/apache-hive-2.1.1-bin/bin/schematool -dbType mysql -initSchema --verbose"
done
# 安装scala
mkdir -p /usr/scala
tar -zxvf  ./scala-2.11.11.tgz -C /usr/scala
# 安装spark
mkdir -p /usr/spark
tar -zxvf ./spark-2.4.3-bin-hadoop2.7.tgz -C /usr/spark
cp /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh.template /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh
echo 'export JAVA_HOME=/usr/java/jdk1.8.0_171' >> /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh
echo 'export SCALA_HOME=/usr/scala/scala-2.11.11' >> /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh
echo 'export HADOOP_HOME=/usr/hadoop/hadoop-2.7.3' >> /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh
echo 'export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop' >> /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh
echo 'export SPARK_MASTER_IP=master' >> /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh
echo 'export SPARK_WORKER_MEMORY=8g' >> /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh
cp /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/slaves.template /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/slaves
cat /dev/null > /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/slaves
for ((i=1; i<index; i++))
do
  echo "slave$i" >> /usr/spark/spark-2.4.3-bin-hadoop2.7/conf/slaves
done
for ((i=1; i<index; i++))
do
  scp -r /usr/spark/spark-2.4.3-bin-hadoop2.7/conf root@slave${i}:/usr/spark/spark-2.4.3-bin-hadoop2.7
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
echo '# hive' >> /etc/profile
echo 'export HIVE_HOME=/usr/hive/apache-hive-2.1.1-bin' >> /etc/profile
echo 'export PATH=$PATH:$HIVE_HOME/bin' >> /etc/profile
echo '# scala' >> /etc/profile
echo 'export SCALA_HOME=/usr/scala/scala-2.11.11' >> /etc/profile
echo 'export PATH=$PATH:$SCALA_HOME/bin' >> /etc/profile
echo '# spark' >> /etc/profile
echo 'export SPARK_HOME=/usr/spark/spark-2.4.3-bin-hadoop2.7' >> /etc/profile
echo 'export PATH=$PATH:$SPARK_HOME/bin' >> /etc/profile
echo 'unset MAILCHECK' >> /etc/profile
source /etc/profile
for ((i=1; i<index; i++))
do
  scp /etc/profile root@slave${i}:/etc/
  ssh root@slave${i} "source /etc/profile"
done
# 格式化节点
hadoop namenode -format



