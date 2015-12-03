package com.skcc.bds.test

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import au.com.bytecode.opencsv.CSVParser

object WordCountMapPartitionTest {
  def main(args: Array[String]) {
    
    val conf = new SparkConf().setAppName("wordCount").setMaster("local[2]")
      // Create a Scala Spark Context.
    val sc = new SparkContext(conf)
    
    val myfile = sc.textFile("src/main/resources/data/2008.csv").cache()
    System.out.println("partition length : " + myfile.partitions.length)
    myfile.mapPartitions(lines => {
           val parser = new CSVParser(',')
           
           //System.out.println("Length : " + lines.length) //
           lines.map(line => {
             parser.parseLine(line).mkString(",")
           })
         }).take(100).foreach(s => println("mapPartitions : " + s))
         
         
     // function list    
     def dropHeader(data: RDD[String]): RDD[String] = {
         data.mapPartitionsWithIndex((idx, lines) => {
           if (idx == 0) {
             lines.drop(1)
           }
           lines
         })
      }

      val withoutHeader: RDD[String] = dropHeader(myfile)
      
      withoutHeader.mapPartitions(lines => {
             val parser = new CSVParser(',')
             lines.map(line => {
               parser.parseLine(line).mkString(",")
             })
           }).take(5).foreach(s=> println ("withoutHeader : " + s))
           
           
      withoutHeader.mapPartitions(lines => {
             val parser=new CSVParser(',')
             lines.map(line => {
               val columns = parser.parseLine(line)
               Array(columns(16)).mkString(",")
             })
           }).countByValue().toList.sortBy(-_._2).foreach((t) => println ("str : " + t._1 + " | num : " + t._2))
  }

  
       

}