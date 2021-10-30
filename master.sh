#!/bin/bash
# 下载资源
yum install -y net-tools
yum install -y vim
yum install -y ntp
wget https://repo.huaweicloud.com/java/jdk/8u171-b11/jdk-8u171-linux-x64.tar.gz
wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.10/zookeeper-3.4.10.tar.gz
wget https://archive.apache.org/dist/hadoop/core/hadoop-2.7.3/hadoop-2.7.3.tar.gz
wget https://repo.mysql.com//mysql57-community-release-el7-11.noarch.rpm

# 基础环境配置
# 配置主机名
hostnamectl set-hostname master
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
# 关掉防火墙
systemctl stop firewalld
systemctl disable firewalld
# 配置环境变量
echo "TZ='Asia/Shanghai'; export TZ" >> /etc/profile



