package com.spark.test


import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats, ShortTypeHints}
import org.json4s.jackson.JsonMethods

import scala.util.parsing.json.JSON
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.parsing.json.JSON

object JsonFile {

  val conf: SparkConf = new SparkConf().setMaster("spark://192.168.137.181:7077").setAppName("JsonFile").setJars(List("target/SparkTest-1.0-SNAPSHOT.jar")).setIfMissing("spark.driver.host", "192.168.1.107")

  val sc = new SparkContext(conf)

  def main(args: Array[String]): Unit = {
    val students = this.readJson("hdfs://192.168.137.181:9000/home/drzon/object.json")
    this.saveFile(students, "hdfs://192.168.137.181:9000/home/drzon/out.json")
  }

  case class Student(name: String, study: Boolean)

  def readJson(path: String): RDD[Student] ={
    val file = sc.textFile(path)
    val rdd = file.map(x => JSON.parseFull(x))
    rdd.collect().foreach(x => x match {
      case Some(map: Map[String, Any]) => println(map)
      case None => println("None")
      case _ => println("no match")
    })
    implicit val formats: AnyRef with Formats = Serialization.formats(ShortTypeHints(List()))

    file.collect().foreach(x => {
      val stu = JsonMethods.parse(x).extract[Student]
      println(s"${stu.name} ${stu.study}")
    })
    val students = file.map(x => {
      implicit val formats: DefaultFormats.type = DefaultFormats
      val stu = JsonMethods.parse(x)
      stu.extract[Student]
    })
    students
  }

  def saveFile(stus: RDD[Student], path: String): Unit = {
    stus.saveAsTextFile(path)
  }
}
