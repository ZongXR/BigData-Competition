package com.spark.test

import java.text.SimpleDateFormat

import org.apache.spark.sql.SparkSession
import java.util.Date

import org.apache.spark.{SparkConf, SparkContext}

object TaobaoData {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local[*]")
    conf.setAppName("TaobaoData")
    val filePath = "C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\taobao100.csv"

    // RDD实现
    /*
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val rdd = sc.textFile(filePath)
    val rdd1 = rdd.filter(x => !x.contains("user_id"))
    val rdd2 = rdd1.map(x => {
      val words = x.split(",")
      val userId = words(0)
      val itemId = words(1)
      val behaviorType = words(2).toInt
      val itemCategory = words(3)
      val date = this.strToDate(words(4))
      val hour = words(5)
      (userId, itemId, behaviorType, itemCategory, date, hour)
    })
    // 每个用户访问次数前50
    val eachTimePerUser = rdd2.groupBy(_._1).mapValues(x => x.toList.length).sortBy(_._2, ascending = false).take(50)
    // 统计独立用户数
    val userNum = rdd2.map(x => x._1).distinct().count()
    // 统计每件商品的购买次数
    val itemPurchase = rdd2.filter(x => x._3 == 4).groupBy(_._2).mapValues(x => x.toList.length).sortBy(_._2, ascending = false)
    // 统计每件商品的收藏次数
    val itemFavor = rdd2.filter(x => x._3 == 3).groupBy(_._2).mapValues(x => x.toList.length).sortBy(_._2, ascending = false)
    // 统计日成交量top20
    val dealNum = rdd2.groupBy(_._5).mapValues(x => x.toList.count(x => x._3 == 4)).top(20)(Ordering.by[(Date,Int),Int](x => x._2))

    println(dealNum.toList)
    sc.stop()
    */

    val spark = SparkSession.builder().config(conf).getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    val df = spark.read.option("header", value = true).csv(filePath)
    df.createOrReplaceTempView("user")
    // 每个用户访问次数前50
    var sql =
      """
        |select user_id,
        |count(1) as cnt
        |from user
        |group by user_id
        |order by cnt desc limit 50
        |""".stripMargin
    // 统计独立用户数
    sql =
      """
        |select count(1) as cnt
        |from (select distinct user_id from user) as unique_user
        |""".stripMargin
    // 统计商品被购买次数
    sql =
      """
        |select item_id,
        |count(1) as cnt
        |from user
        |where behavior_type = '4'
        |group by item_id
        |order by cnt desc
        |limit 20
        |""".stripMargin
    // 统计商品被收藏的次数
    sql =
      """
        |select item_id,
        |count(1) as cnt
        |from user
        |where behavior_type = 3
        |group by item_id
        |order by cnt desc
        |limit 20
        |""".stripMargin
    // 统计成交量top20
    sql =
      """
        |select date,
        |count(1) as cnt
        |from user
        |where behavior_type = 4
        |group by date
        |order by cnt desc
        |limit 20
        |""".stripMargin

    val df2 = spark.sql(sql)
    df2.show()
    spark.stop()
  }

  def strToDate(strDate: String): Date ={
    val format = new SimpleDateFormat("yyyy/MM/d")
    return format.parse(strDate)
  }

}
