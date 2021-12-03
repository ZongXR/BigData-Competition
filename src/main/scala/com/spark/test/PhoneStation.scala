package com.spark.test

import java.text.SimpleDateFormat
import java.util.Date
import java.sql.Date

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._

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
    /*
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
     */

    // DataFrame做法
    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._
    val schemaStation: StructType = Encoders.product[Station].schema
    val schemaRela: StructType = Encoders.product[UserStationRela].schema
    val station: Dataset[Station] = spark.read.option("header", value = false).option("sep", ",").schema(schemaStation).csv(locPath).as[Station]
    val relaA: Dataset[Row] = spark.read.option("header", value = false).option("sep", ",").option("timestampFormat", "yyyyMMddHHmmss").schema(schemaRela).csv(stationAPath)
    val relaB: Dataset[Row] = spark.read.option("header", value = false).option("sep", ",").option("timestampFormat", "yyyyMMddHHmmss").schema(schemaRela).csv(stationBPath)
    val relaC: Dataset[Row] = spark.read.option("header", value = false).option("sep", ",").option("timestampFormat", "yyyyMMddHHmmss").schema(schemaRela).csv(stationCPath)

    val rela = relaA.union(relaB).union(relaC)
    val ds1: Dataset[(String, Long, String, Int)] = rela.map(x => {
      if (x.getInt(3) == 0)
        (x.getString(0), x.getTimestamp(1).getTime, x.getString(2), x.getInt(3))
      else
        (x.getString(0), -x.getTimestamp(1).getTime, x.getString(2), x.getInt(3))
    })
    val ds2: DataFrame = ds1.groupBy("_1", "_3").agg(sum("_2") as "_2")
    val ds3: DataFrame = station.join(ds2, station("stationId") === ds2("_3"), "inner")
    val ds4 = ds3.map(x => {
      (x.getString(3), x.getString(0), x.getDouble(1), x.getDouble(2), x.getLong(5))
    })
    val ds5 = ds4.withColumn("rr", row_number().over(Window.partitionBy("_1").orderBy($"_5".desc)))
    val ds6 = ds5.where(ds5("rr") <= 2)
    val ds7 = ds6.map(x => {
      (x.getString(0), x.getDouble(2), x.getDouble(3), x.getLong(4))
    })

    ds7.show()
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