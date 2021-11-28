package com.spark.test

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.spark.{SparkConf, SparkContext}

object PhoneStation {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local[*]")
    conf.setAppName("PhoneStation")

    val sc = new SparkContext(conf)
    val loc = sc.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\loc")
    val stationA = sc.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\baseA")
    val stationB = sc.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\baseB")
    val stationC = sc.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\baseC")

    val station = stationA.union(stationB).union(stationC)
    val rdd1 = station.map(x => this.readLine(x))
    val rdd2 = rdd1.map(x => this.setNegSecond(x))
    val rdd3 = rdd2.map(x => {
      ((x._1, x._3), x._2)
    })
    val rdd4 = rdd3.reduceByKey(_ + _)
    val rdd5 = rdd4.map(x => {
      (x._1._1, x._1._2, x._2)
    })
    val rdd6 = rdd5.groupBy(_._1).mapValues(x => {
      val a = x.toList
      val b = a.sortBy(_._3)
      val c = b.reverse
      val d = c.take(2)
      d
    })
    val rdd7 = rdd6.flatMap(x => x._2)
    val rdd8 = rdd7.map(x => {
      (x._2, (x._1, x._3))
    })

    val loc1 = loc.map(x => {
      val words = x.split(",")
      (words(0), (words(1).toDouble, words(2).toDouble))
    })

    val rdd9 = rdd8.join(loc1)
    val rdd10 = rdd9.map(x => {
      val value = x._2
      (value._1._1, value._1._2, value._2._1, value._2._2)
    })
    val rdd11 = rdd10.groupBy(_._1).mapValues(x => {
      val a = x.toList
      a(0).toString() + "," + a(1).toString()
    })
    val rdd12 = rdd11.map(x => x.productIterator.mkString("=>"))
    println(rdd12.collect().toList)
    rdd12.saveAsTextFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\result")
    sc.stop()
  }

  /**
   * 读入一行字符串，整理成对应格式
   * @param line 一行字符串
   * @return (手机号, 秒数, 基站ID, 连接)
   */
  def readLine(line: String): (String, Long, String, Int) ={
    val words = line.split(",")
    val phoneNum: String = words(0)
    val time: Long = this.str2Time(words(1)).getTime() / 1000
    val stationId: String = words(2)
    val connect: Int = words(3).toInt
    return (phoneNum, time, stationId, connect)
  }

  /**
   * 把进入连接的秒数置成负数
   * @param x 手机号, 时刻, 基站ID, 连接
   * @return 手机号, 时刻, 基站ID
   */
  def setNegSecond(x: (String, Long, String, Int)): (String, Long, String) ={
    if (x._4 == 0)
      return (x._1, x._2, x._3)
    else
      return (x._1, -x._2, x._3)
  }

  /**
   * 字符串转日期
   * @param word 字符串
   * @return 日期
   */
  def str2Time(word: String): Date ={
    val format = new SimpleDateFormat("yyyyMMddHHmmss")
    val date: Date = format.parse(word)
    return date
  }

  /**
   * 日期差
   * @param early 较早日期
   * @param late 较晚日期
   * @return 相差秒数
   */
  def getDeltaSeconds(early: Date, late: Date): Long ={
    val l = late.getTime() - early.getTime()
    return l / 1000
  }

}
