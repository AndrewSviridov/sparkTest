package com.skcc.bds.test

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming.kafka._
//import com.skcc.StreamingExamples

object KafkaTest {
  def main(args: Array[String]) {
      // parameters
    val zkQuorum = "203.235.199.137:2181"
    val group = "spark-readers"
    val topics = "test"
    val numThreads = 1
  
      // initialize spark
      val sparkConf = new SparkConf()
        .setAppName("KafkaTest")
        .setMaster("local[2]")
           
      val ssc = new StreamingContext(sparkConf, Seconds(2))
 
      val topicMap = topics.split(",").map((_, 1)).toMap
      val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)
      lines.print
  

      ssc.start()
      ssc.awaitTermination()
  }
}