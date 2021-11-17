package com.spark.test

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("Word Count")
    conf.setMaster("spark://192.168.137.181:7077")
    conf.setJars(List("target/WordCount-1.0-SNAPSHOT.jar"))
    conf.setIfMissing("spark.driver.host", "192.168.1.107")

    val session = SparkSession.builder.config(conf).getOrCreate()
    val rdd = session.sparkContext.textFile("hdfs://192.168.137.181:9000/home/drzon/README.md")
    val lines = rdd.flatMap(x => x.split(" "))
      .map(x => (x, 1))
      .reduceByKey((a, b) => (a + b))
      .collect().toList

    println(lines)
  }

}
