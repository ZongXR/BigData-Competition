package com.spark.test

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local[*]")
    conf.setAppName("WordCount")
    val filePath = "C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\LICENSE"
    val puncPath = "C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\punctuations"

    // RDD做法
    /*
    val sc = new SparkContext(conf)
    val punctuations: RDD[String] = sc.textFile(puncPath)
    val punc: Array[Char] = (' ' :: punctuations.flatMap(x => x.split("")).map(x => x.toList.head).collect().toList).toArray

    val file: RDD[String] = sc.textFile(filePath)
    val result = file.flatMap(x => x.split(punc)).map(x => x.trim()).filter(x => x.length() > 0).map(x => (x, 1)).reduceByKey(_ + _).sortBy(_._2, ascending = false)
    println(result.collect().toList)
     */

    // Dataset做法

    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._
    val file = spark.read.textFile(filePath)
    val punctuations: Dataset[String] = spark.read.textFile(puncPath)
    val punc: List[Char] = punctuations.flatMap(x => x.split("")).collect().toList.map(x => x.charAt(0))
    val puncs: Array[Char] = (' ' :: punc).toArray

    val words: Dataset[String] = file.flatMap(x => x.split(puncs)).map(x => x.trim()).filter(x => x.length() > 0)
    val ds = words.map(x => (x, 1)).groupBy("_1").count().orderBy($"count".desc)
    ds.show()

    // Spark SQL做法
    /*
    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._
    val punctuations: Dataset[String] = spark.read.textFile(puncPath)
    val punc: List[Char] = punctuations.flatMap(x => x.split("")).collect().toList.map(x => x.charAt(0))
    val puncs: Array[Char] = (' ' :: punc).toArray

    val file: Dataset[String] = spark.read.textFile(filePath)
    val words: Dataset[String] = file.flatMap(x => x.split(puncs)).map(x => x.trim).filter(x => x.length() > 0)
    words.createOrReplaceTempView("words")
    val df: DataFrame = spark.sql(
      """
        |select value,
        |count(1) as cnt
        |from words
        |group by value
        |order by cnt desc
        |""".stripMargin)

    df.show()
     */

  }

}
