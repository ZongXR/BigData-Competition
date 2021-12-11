package com.spark.test

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object PeopleAgeHeight {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("PeopleAgeHeight")
    conf.setMaster("local[*]")

    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")

    //    将1、2题目中的数据，按编号进行关联join
    val ages = sc.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\peopleage.txt")
    val heights = sc.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\peopleinfo.txt")
    val ages1 = ages.map(x => x.split(" ")).map(x => (x(0).toInt, x(1).toInt))
    val heights1 = heights.map(x => x.split(" ")).map(x => (x(0).toInt, (x(1), x(2).toInt)))
    val data = ages1.join(heights1).map(x => (x._1, x._2._1, x._2._2._1, x._2._2._2))   // id, age, gender, height

    //    1. 统计男性不同年龄下的身高范围
    val maleAgesHeights = data.filter(x => x._3 == "M").map(x => (x._2, x._4)).groupByKey().mapValues(x => (x.toList.min, x.toList.max)).sortBy(_._1, ascending = true)
    println(maleAgesHeights.collect().toList)

    //      2. 统计女性年龄最多的年龄下，平均身高
    val femaleMaxAgeHeights = data.filter(x => x._3 == "F").map(x => (x._2, x._4)).groupByKey()
    val fMaxAgeHeights = femaleMaxAgeHeights.top(1).head
    val femaleMaxAge = fMaxAgeHeights._1
    val femaleMaxAgeAvgHeight = fMaxAgeHeights._2.toList.sum.toDouble / fMaxAgeHeights._2.count(x => true)
    println(femaleMaxAge, femaleMaxAgeAvgHeight)
    println(femaleMaxAgeHeights.mapValues(x => x.toList.sum.toDouble / x.toList.count(x => true)).top(1).toList.head)

    //    3. 同年龄下，男性比女性的平均身高高多少
    val heightDelta = data.map(x => (x._2, (x._3, x._4))).groupByKey().mapValues(x => {
      val maleHeights = x.filter(a => a._1 == "M").map(a => a._2)
      val femaleHeights = x.filter(a => a._1 == "F").map(a => a._2)
      (maleHeights.sum.toDouble / maleHeights.count(a => true)) - (femaleHeights.sum.toDouble / femaleHeights.count(a => true))
    }).sortBy(_._1, ascending = true)
    println(heightDelta.collect().toList)

    //    4. 年龄超过50岁，性别为男的，身高总和为多少，平均身高为多少
    val male50Heights = data.filter(x => x._2 > 50 && x._3 == "M").map(x => x._4)
    println(male50Heights.sum(), male50Heights.mean())

    sc.stop()

  }

}
