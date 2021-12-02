package com.spark.test

import java.text.SimpleDateFormat
import java.util.Date
import java.sql.Date

import org.apache.spark.sql.{DataFrame, Dataset, Encoders, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object PhoneStation {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local[*]")
    conf.setAppName("PhoneStation")

    val locPath = "C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\loc"
    val stationAPath = "C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\baseA"
    val stationBPath = "C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\baseB"
    val stationCPath = "C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\baseC"

    // RDD做法
    /*
    val sc = new SparkContext(conf)
    val loc = sc.textFile(locPath)
    val stationA = sc.textFile(stationAPath)
    val stationB = sc.textFile(stationBPath)
    val stationC = sc.textFile(stationCPath)

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
     */

    // SparkSQL做法
    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._
    val schemaStation = Encoders.product[Station].schema
    val schemaRela = Encoders.product[UserStationRela].schema
    val loc: Dataset[Station] = spark.read.option("header", value = false).option("sep", ",").schema(schemaStation).csv(locPath).as[Station]
    loc.createOrReplaceTempView("loc")

    val relaA: Dataset[UserStationRela] = spark.read.option("header", value = false).option("sep", ",").option("timestampFormat", "yyyyMMddHHmmss").schema(schemaRela).csv(stationAPath).as[UserStationRela]
    val relaB: Dataset[UserStationRela] = spark.read.option("header", value = false).option("sep", ",").option("timestampFormat", "yyyyMMddHHmmss").schema(schemaRela).csv(stationBPath).as[UserStationRela]
    val relaC: Dataset[UserStationRela] = spark.read.option("header", value = false).option("sep", ",").option("timestampFormat", "yyyyMMddHHmmss").schema(schemaRela).csv(stationCPath).as[UserStationRela]
    val rela: Dataset[UserStationRela] = relaA.union(relaB).union(relaC)
    rela.createOrReplaceTempView("rela")

    val sql =
      """
        |select phoneNum,
        |second,
        |longitude,
        |latitude
        |from loc as l
        |inner join (
        |select t.phoneNum as phoneNum,
        |t.stationId as stationId,
        |t.second as second
        |from (
        |select phoneNum,
        |stationId,
        |second,
        |row_number() over(partition by phoneNum order by second desc) as rr
        |from (
        |select phoneNum,
        |stationId,
        |sum(second_stamp) as second
        |from (
        |(select phoneNum,
        |-to_unix_timestamp(date) as second_stamp,
        |stationId
        |from rela
        |where isIn = 1
        |) union (
        |select phoneNum,
        |to_unix_timestamp(date) as second_stamp,
        |stationId
        |from rela
        |where isIn = 0
        |)
        |)
        |group by phoneNum, stationId
        |)
        |) as t
        |where t.rr <= 2
        |) as r
        |on l.stationId = r.stationId
        |""".stripMargin

    val df: DataFrame = spark.sql(sql)
    df.show()
    spark.stop()
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
  def str2Time(word: String): java.util.Date ={
    val format = new SimpleDateFormat("yyyyMMddHHmmss")
    val date: java.util.Date = format.parse(word)
    return date
  }

  /**
   * 日期差
   * @param early 较早日期
   * @param late 较晚日期
   * @return 相差秒数
   */
  def getDeltaSeconds(early: java.util.Date, late: java.util.Date): Long ={
    val l = late.getTime() - early.getTime()
    return l / 1000
  }

}


case class Station(
                  stationId: String,
                  longitude: Double,
                  latitude: Double
                  )


case class UserStationRela(
                          phoneNum: String,
                          date: java.sql.Timestamp,
                          stationId: String,
                          isIn: Int
                          )