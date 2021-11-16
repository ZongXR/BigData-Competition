package com.spark.test

import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("Word Count")
    conf.setMaster("spark://192.168.137.181:7077")
    conf.setJars(List("target/WordCount-1.0-SNAPSHOT.jar"))
    conf.setIfMissing("spark.driver.host", "192.168.1.107")
    val sc = new SparkContext(conf)

    val file = "hdfs://192.168.137.181:9000/home/drzon/README.md"
    val lines = sc.textFile(file)
    val words = lines.flatMap(_.split("\\s+"))
    val wordCount = words.countByValue()

    println(wordCount)
  }

}
