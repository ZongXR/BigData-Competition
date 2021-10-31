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
hostnamectl set-hostname slave
# 关掉防火墙
systemctl stop firewalld
systemctl disable firewalld
# 同步时钟
ntpdate master
whereis ntpdate
echo "*/30 10-17 * * * /usr/sbin/ntpdate master" >> /var/spool/cron/root
# 配置环境变量

