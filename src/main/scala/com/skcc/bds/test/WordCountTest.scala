/**
 * Illustrates flatMap + countByValue for wordcount.
 */
package com.skcc.bds.test

import org.apache.spark._
import org.apache.spark.SparkContext._

object WordCount {
    def main(args: Array[String]) {
      val inputFile = "D:/history.txt"
      val outputFile = "D:/out.txt"
      val conf = new SparkConf().setAppName("wordCount").setMaster("local[2]")
      // Create a Scala Spark Context.
      val sc = new SparkContext(conf)
      // Load our input data.
     // val input =  sc.textFile(inputFile)
      val input =  sc.textFile("hdfs://bdsdevname01:8020/tmp/data")
      // Split up into words.
      val words = input.flatMap(line => line.split(" "))
      // Transform into word and count.
      val counts = words.map(word => (word, 1)).reduceByKey{case (x, y) => x + y}
      // Save the word count back out to a text file, causing evaluation.
      //counts.saveAsTextFile(outputFile)
       println(counts.collect().mkString(","))
      
    }
}
