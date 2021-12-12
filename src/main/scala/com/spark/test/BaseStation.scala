package com.spark.test

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object BaseStation {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("BaseStation")
    conf.setMaster("local[*]")

    val sc = new SparkContext(conf)
    val rdd = sc.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\text")
    val rdd1 = rdd.map(x => {
      val words = x.split(",")
      val phone = words(0)
      val time = words(1).toLong
      val lacId = words(2)
      val status = words(3).toInt
      (phone, time, lacId, status)
    })

    val rdd2 = rdd1.map(x => {
      val phone = x._1
      var time = x._2
      val lacId = x._3
      val status = x._4
      if (status == 1)
        time = -time
      ((phone, lacId), time)
    })

    val rdd3 = rdd2.reduceByKey((a, b) => a + b)
    val rdd4 = rdd3.map(x => {
      val phoneLac = x._1
      val time = x._2
      (phoneLac._2, (phoneLac._1, time))
    })

    val loc = sc.textFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\loc_info")

    val lac1 = loc.map(x => {
      val words = x.split(",")
      val lacId = words(0)
      val jingdu = words(1).toDouble
      val weidu = words(2).toDouble
      val leixing = words(3).toInt
      (lacId, (jingdu, weidu))
    })

    val rdd5 = lac1.join(rdd4)

    val rdd6 = rdd5.map(x => {
      val jingdu = x._2._1._1
      val weidu = x._2._1._2
      val phone = x._2._2._1
      val time = x._2._2._2
      (phone, time, jingdu, weidu)
    })

    val rdd7 = rdd6.groupBy(x => x._1).mapValues(x => {
      val a = x.toList
      val b = a.sortBy(_._2)
      val c = b.reverse
      val d = c.take(2)
      d
    })

    val rdd8 = rdd7.flatMap(x => x._2)

    val rdd9 = rdd8.map(x => {
      x.productIterator.mkString(",")
    })

    rdd9.saveAsTextFile("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\out1")

    println(rdd9.collect().toList)

    sc.stop()
  }

}
