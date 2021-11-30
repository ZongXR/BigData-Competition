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

//    val spark = SparkSession.builder().config(conf).getOrCreate()
//    val df = spark.read.option("header", value = true).csv(filePath)
//    df.createOrReplaceTempView("user")
//    val sql =
//      """
//        |select user_id,
//        |count(1) as cnt
//        |from user
//        |group by user_id
//        |order by cnt desc
//        |""".stripMargin
//    val df1 = spark.sql(sql)
//
//    println(df1.show())
  }

  def strToDate(strDate: String): Date ={
    val format = new SimpleDateFormat("yyyy/MM/d")
    return format.parse(strDate)
  }

}
