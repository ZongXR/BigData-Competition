package com.qingjiao.staff

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object MobileLocation {
  def main(args: Array[String]): Unit = {
    // 设置参数
    val conf = new SparkConf()
      .setAppName("MobileLocation")
      // 本地化运行，CPU使用的线程数，除了数据读取，一般都是2以上
//      .setMaster("local[2]")

    //以参数形式传递给SparkContext，程序入口
    val sc = new SparkContext(conf)

    //第一步：读取手机用户数据-拆分
    val file:RDD[String] = sc.textFile("/root/phonestation/testdata/user.txt")
    //定义phoneAndLacAndTime用于接收元组，map函数指定一个参数line，line代表了每行数据
    val phoneAndLacAndTime = file.map(line => {
      //数据按照逗号拆分，拆分的数据形成了数组()
      val words = line.split(",")
      //下标为0的是手机号phone
      val phone = words(0)
      //下标为1的是时间time，转成long类型，后续对时间进行计算
      val time = words(1).toLong
      //下标为2的是基站ID lac
      val lac = words(2)
      //下标为3的是事件类型标识eventType，只有接入1，离开0
      val eventType = words(3).toInt
      //判断连接时间和断开时间,得到时间time_long
      val time_long = if(eventType==1) -time else time
      //生成元组
      ((phone,lac),time_long)
    })

    // 第二步：时间和
    //根据键值进行聚合
    //根据key分组 累加value
    val sumedPhoneAndLacAndTime = phoneAndLacAndTime.reduceByKey(_+_)
    //使用map算子获取到信息，转换数据格式
    val lacAndPhoneAndTime = sumedPhoneAndLacAndTime.map(x =>{
      //根据之前的元组信息，拿到手机号phone
      val phone = x._1._1
      //拿到基站ID lac
      val lac = x._1._2
      //获取到用户在这个基站的停留时间 timeSum
      val timeSum = x._2
      //生成元组
      (lac,(phone,timeSum))
    })

    //第三步：经纬度
    val lacInfo = sc.textFile("/root/phonestation/testdata/info.txt")
    //读取的基站数据指定一个参数line
    val lacAndXY = lacInfo.map(line =>{
      //每行数据按照逗号拆分
      val fields = line.split(",")
      //获取基站ID
      val lac = fields(0)
      //获取经度
      val x = fields(1)
      //获取纬度
      val y = fields(2)
      //生成包含基站ID、经纬度的元组
      (lac,(x,y))
    })

    //第四步：join操作
    //两张表的根据基站ID进行join操作
    val join = lacAndPhoneAndTime.join(lacAndXY)

    val res = join.map(x =>{
      //手机号
      val phone = x._2._1._1
      //基站ID
      val lac = x._1
      //停留时间
      val time = x._2._1._2
      //经纬度
      val xy = x._2._2
      (phone,time,lac,xy)
      //时长降序排序，top2
      //mapValues算子直接操作值
      //toList 数据转换成list之后，才能排序
      //reverse 倒叙/降序
      //take() 获取几个数据
    }).groupBy(_._1).mapValues(_.toList.sortBy(_._3).reverse.take(1))
    // 返回数组，打印结果
    println(res.collect().toBuffer)
    // 保存至文件
    res.saveAsTextFile("/root/phonestation/testdata/output")
    // 释放资源 关闭spark
    sc.stop()
  }

}
