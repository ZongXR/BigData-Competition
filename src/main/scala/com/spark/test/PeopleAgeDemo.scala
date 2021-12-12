package com.spark.test

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

object PeopleAgeDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("AgeDemo")
    conf.setMaster("local[*]")

    /*
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")

    //    第一列为编号  第二列为年龄
    val rdd = sc.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\peopleage.txt")
    val rdd1 = rdd.map(x => {
      val words = x.split(" ")
      (words(0), words(1).toInt)
    })

    //    1.  一共有多少个记录
    val cnt = rdd1.count()
    println(cnt)

    //      2.  所有记录的年龄总和是多少
    val sumAge = rdd1.map(x => x._2).sum()
    println(sumAge)

    //      3.  平均年龄是多少
    println(rdd1.map(x => x._2).mean())

    //      4.  最大、最小年龄是多少
    val maxAge = rdd1.map(x => x._2).max()
    val minAge = rdd1.map(x => x._2).min()
    println(maxAge, minAge)

    //    5.  统计不同年龄的人数，并按年龄排序
    val ageCount = rdd1.map(x => (x._2, 1)).groupByKey().mapValues(x => x.toList.count(x => true)).sortBy(_._1, ascending = false)
    println(ageCount.collect().toList)

    //    6.  统计最大5个年龄的人数、统计最小10个年龄的人数
    val topCount = ageCount.top(5)
    println(topCount.toList)
    println(ageCount.sortBy(_._1, ascending = true).take(10).toList)

    //    7.  top5年龄数的人员编号是多少，并按编号排序
    val top5AgeCount = topCount.map(x => x._1)
    val id = rdd1.filter(x => top5AgeCount.contains(x._2))
    println(id.map(x => x._1).sortBy(x => x).collect().toList)

    //    8.  从年龄最小开始累计人数，0-多少岁的人员占比为20%
    val num = cnt * 0.2
    val ages = rdd1.sortBy(x => x._2, ascending = true).map(x => x._2)
    val ages1 = ages.collect().toList
    val ages2 = ages1.filter(x =>
      ages.filter(a => a <= x).count() >= num
    )
    println(ages2.head)

    sc.stop()
     */

    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._
    spark.sparkContext.setLogLevel("WARN")

    //    第一列为编号  第二列为年龄
    val ds = spark.read.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\peopleage.txt")
    val ds1 = ds.map(x => (x.split(" ").toList.head.toInt, x.split(" ").toList(1).toInt))
    ds1.show()

    //    1.  一共有多少个记录
    ds1.createOrReplaceTempView("ages")
    val sql1 =
      """
        |select count(1)
        |from ages
        |""".stripMargin
    spark.sql(sql1).show()

    //      2.  所有记录的年龄总和是多少
    val sql2 =
      """
        |select sum(_2)
        |from ages
        |""".stripMargin
    spark.sql(sql2).show()

    //      3.  平均年龄是多少
    val sql3 =
      """
        |select avg(_2)
        |from ages
        |""".stripMargin
    spark.sql(sql3).show()

    //      4.  最大、最小年龄是多少
    val sql4 =
      """
        |select max(_2), min(_2)
        |from ages
        |""".stripMargin
    spark.sql(sql4).show()

    //    5.  统计不同年龄的人数，并按年龄排序
    val sql5 =
      """
        |select _2 as age,
        |count(1) as cnt
        |from ages
        |group by _2
        |order by age
        |""".stripMargin
    spark.sql(sql5).show()

    //    6.  统计最大5个年龄的人数、统计最小10个年龄的人数
    val sql61 =
      """
        |select _2 as age,
        |count(1) as cnt
        |from ages
        |group by _2
        |order by age desc
        |limit 5
        |""".stripMargin
    spark.sql(sql61).show()

    val sql62 =
      """
        |select _2 as age,
        |count(1) as cnt
        |from ages
        |group by _2
        |order by age
        |limit 10
        |""".stripMargin
    spark.sql(sql62).show()

    //    7.  top5年龄数的人员编号是多少，并按编号排序
    val sql7 =
      """
        |select _1 as ids
        |from ages
        |where _2 in (
        |select _2 as age
        |from ages
        |group by _2
        |order by count(1) desc
        |limit 5
        |)
        |order by ids
        |""".stripMargin
    spark.sql(sql7).show()

    //    8.  从年龄最小开始累计人数，0-多少岁的人员占比为20%
    val sql81 =
      s"""
        |select *
        |from (
        |select age,
        |sum(cnt) over(order by age) as acc_cnt
        |from (
        |select _2 as age,
        |count(1) as cnt
        |from ages
        |group by _2
        |order by _2
        |)
        |)
        |where acc_cnt >= ${0.2 * ds1.count()}
        |limit 1
        |""".stripMargin
    spark.sql(sql81).show()

    val sql82 =
      s"""
        |select distinct *
        |from (
        |select _2 as age,
        |count(1) over(order by _2) as acc_cnt
        |from ages
        |order by _2
        |)
        |where acc_cnt >= ${0.2 * ds1.count()}
        |limit 1
        |""".stripMargin
    spark.sql(sql82).show()

    spark.stop()

  }

}
