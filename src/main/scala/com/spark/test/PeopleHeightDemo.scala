package com.spark.test

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.control.Breaks

object PeopleHeightDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("PeopleInfoDemo")
    conf.setMaster("local[*]")

    /*
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")

    //    读取peopleinfo.txt文件
    //    第一列为编号  第二列为性别 M为男性 F为女性  第三列为身高
    val rdd = sc.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\peopleinfo.txt")
    val rdd1 = rdd.map(x => {
      val words = x.split(" ")
      (words(0).toInt, words(1), words(2).toInt)
    })

    //    1.  一共有多少个记录
    println(rdd1.count())

    //      2.  男人和女人各有多少个
    val genderCount = rdd1.map(x => (x._2, 1)).reduceByKey((a, b) => a + b)
    println(genderCount.collect().toList)

    //      3.  男人和女人的最大最小身高是多少
    val maxHeight = rdd1.map(x => (x._2, x._3)).groupByKey().mapValues(x => x.toList.max)
    val minHeight = rdd1.map(x => (x._2, x._3)).groupByKey().mapValues(x => x.toList.min)
    println(maxHeight.collect().toList)
    println(minHeight.collect().toList)

    //      4.  男人和女人的平均身高是多少
    val meanHeight = rdd1.map(x => (x._2, x._3)).groupByKey().mapValues(x => x.toList.sum.toDouble / x.toList.count(x => true))
    println(meanHeight.collect().toList)

    //    5.  不同身高的男性和女性数量是多少
    val heightCount = rdd1.map(x => (x._3, x._2, 1)).groupBy(_._1).mapValues(x => {
      (x.count(x => x._2 == "M"), x.count(x => x._2 == "F"))
    }).sortBy(_._1, ascending = true)
    println(heightCount.collect().toList)

    //      6.  最高5个身高的男性和女性数量
    println(heightCount.top(5).toList)

    //      7.  哪个身高的男性人数最多 哪个身高的女性人数最多
    val maleMaxCount = heightCount.sortBy(_._2._1, ascending = false)
    val maleMaxHeightCount = maleMaxCount.take(1).toList.head._2._1
    val maleHCount = maleMaxCount.filter(x => x._2._1 == maleMaxHeightCount).map(x => x._1)
    println(maleHCount.collect().toList)

    val femaleMaxCount = heightCount.sortBy(_._2._2, ascending = false)
    val femaleMaxHeightCount = femaleMaxCount.take(1).toList.head._2._2
    val femaleHCount = femaleMaxCount.filter(x => x._2._2 == femaleMaxHeightCount).map(x => x._1)
    println(femaleHCount.collect().toList)

    //    8.  以男性的平均身高为中心，从多少到多少身高的男性数量占总数量的80%
    val maleHeights = rdd1.filter(x => x._2 == "M").map(x => x._3).sortBy(x => x, ascending = true)
    val maleHeightsAvg = maleHeights.mean()
    val cnt = 0.8 * maleHeights.count()
    var lower = maleHeightsAvg.toInt
    var higher = lower + 1
    val brea = new Breaks()
    brea.breakable(
      for (i <- 0 to 100){
        lower = lower - i
        higher = higher + i
        if (maleHeights.filter(x => x >= lower && x <= higher).count() >= cnt){
          brea.break()
        }
      }
    )
    println(lower, higher)

    sc.stop()
     */

    val spark = SparkSession.builder().config(conf).getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    //    读取peopleinfo.txt文件
    //    第一列为编号  第二列为性别 M为男性 F为女性  第三列为身高
    val ds = spark.read.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\peopleinfo.txt")
    import spark.implicits._
    val ds1 = ds.map(x => {
      val words = x.split(" ")
      (words(0).toInt, words(1), words(2).toInt)
    })
    ds1.createOrReplaceTempView("heights")

    //    1.  一共有多少个记录
    val sql1 =
      """
        |select count(1)
        |from heights
        |""".stripMargin
    spark.sql(sql1).show()

    //      2.  男人和女人各有多少个
    val sql2 =
      """
        |select _2 as gender,
        |count(1) as cnt
        |from heights
        |group by _2
        |""".stripMargin
    spark.sql(sql2).show()

    //      3.  男人和女人的最大最小身高是多少
    val sql3 =
      """
        |select _2 as gender,
        |max(_3) as max_height,
        |min(_3) as min_height
        |from heights
        |group by _2
        |""".stripMargin
    spark.sql(sql3).show()

    //      4.  男人和女人的平均身高是多少
    val sql4 =
      """
        |select _2 as gender,
        |avg(_3) as avg_height
        |from heights
        |group by _2
        |""".stripMargin
    spark.sql(sql4).show()

    //    5.  不同身高的男性和女性数量是多少
    val sql5 =
      """
        |select *
        |from (
        |select _3 as height,
        |_2 as gender,
        |count(1) as cnt
        |from heights
        |group by height, gender
        |) pivot (
        |sum(cnt)
        |for gender in ('M', 'F')
        |)
        |order by height
        |""".stripMargin
    spark.sql(sql5).show()

    //      6.  最高5个身高的男性和女性数量
    val sql6 =
      """
        |select *
        |from (
        |select _3 as height,
        |_2 as gender,
        |count(1) as cnt
        |from heights
        |group by height, gender
        |) pivot(
        |sum(cnt)
        |for gender in ('M', 'F')
        |)
        |order by height desc
        |limit 5
        |""".stripMargin
    spark.sql(sql6).show()

    //      7.  哪个身高的男性人数最多 哪个身高的女性人数最多
    val sql71 =
      """
        |select height, cnt
        |from (
        |select *,
        |rank(cnt) over (order by cnt desc) as rnk
        |from (
        |select _3 as height,
        |count(1) as cnt
        |from heights
        |where _2 = 'M'
        |group by _3
        |order by cnt desc
        |)
        |)
        |where rnk = 1
        |""".stripMargin
    spark.sql(sql71).show()

    val sql72 =
      """
        |select _3 as height,
        |count(1) as cnt
        |from heights
        |where _2 = 'F'
        |group by height
        |order by cnt desc
        |limit 1
        |""".stripMargin
    spark.sql(sql72).show()

    spark.stop()

  }

}
