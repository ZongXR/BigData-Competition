package com.spark.test

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DateType, FloatType, IntegerType, LongType, StringType, StructField, StructType}

object DatasetDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("DatasetDemo")
    conf.setMaster("local[*]")

    val spark = SparkSession.builder().config(conf).getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._

    val df: DataFrame = spark.read.json("C:\\Users\\DrZon\\IdeaProjects\\Install-BigData\\data\\emp.json")

    // 将DataFrame转为Dataset
    val ds: Dataset[Emp] = df.as[Emp]

    //    Dataset转DataFrame
    val df1: DataFrame = ds.select(
      ds("name"),
      current_date(),
      current_timestamp(),
      rand(),
      round(ds("salary"), 2),
      concat(ds("gender"), ds("age")),
      concat_ws(" ", ds("gender"), ds("age"))
    )
    df1.show()

    val df2: DataFrame = ds.toDF()
    df2.show()

    ds.createOrReplaceTempView("emp")
    val df3: DataFrame = spark.sql(
      """
        |select
        |first(name) as name,
        |current_date() as c_date,
        |max(salary) as salary,
        |gender
        |from emp
        |where age < 30
        |group by gender
        |""".stripMargin)
    df3.show()

    // Dataset转RDD
    val rdd: RDD[Emp] = ds.rdd
    println(rdd.collect().toList)

    // DataFrame转RDD
    val rdd3: RDD[Row] = df3.rdd
    println(rdd3.collect().toList)

    // RDD转Dataset
    rdd.toDS().show()

    // RDD转DataFrame
    rdd.toDF().show()
    val schema = StructType(Array(
      StructField("name",StringType,nullable = true),
      StructField("c_date",DateType,nullable = true),
      StructField("salary",LongType,nullable = true),
      StructField("gender",StringType,nullable = true)
    ))
    spark.createDataFrame(rdd3, schema).show()

    spark.stop()
  }

}

case class Emp(
         name: String,
         age: Long,
         depId: Long,
         gender: String,
         salary: Long)
