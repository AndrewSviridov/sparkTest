package com.skcc.bds.rts

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.scheduler.{StreamingListenerBatchCompleted, StreamingListener}

import scala.collection.mutable.ArrayBuffer

import com.skcc.bds.rts.rule.RuleRunner

object RealTimeApp1 {
 // def maptest(s1:String, s2:String)
  def main(args: Array[String]) {
    val zkQuorum = "192.168.193.129:2181"
    val group = "realtime-group"
    val topics = "realtime"
    val numThreads = 1
    
    val drl = "D:/00.Source/600.Scala/sparkTest/src/main/resources/rule_event_1.drl"
  
    System.out.println("tttt")
    // initialize spark
    val sparkConf = new SparkConf()
      .setAppName("RealTimeAppTest")
      //.setMaster("local[2]")
           
      val ssc = new StreamingContext(sparkConf, Seconds(2))
    
      ssc.checkpoint("src/main/resources/data/checkpoint/realtime")
 
      ssc.addStreamingListener(new StreamingListener() {
        override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
          System.out.println("Batch Completed")
        }
      })
 
      val topicMap = topics.split(",").map((_, 1)).toMap
      val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2).cache
      val counts = lines.map(word => (word, 1))
      
      if(args(0) == "master") {
        val ksession = RuleRunner.makeRuleKieBase(drl)
        

        System.out.println ("Running [MASTER] MODE")
        lines.foreachRDD { rdd => 
//          arr ++= rdd.collect()
          rdd.collect.foreach { x => RuleRunner.runRule(x,ksession) }
          //System.out.println("Buffer Len 1: " + arr.length)
//          rdd.foreach(println)
        }
        lines.print()
      }
      else {
        System.out.println ("Running [LOCAL] MODE")
        lines.foreachRDD { rdd => 
          rdd.mapPartitions(iter => {
            val ksession = RuleRunner.makeRuleKieBase(drl)
            iter.map(item => {
              RuleRunner.runRule(item,ksession)
              item
            })
          }).count
          //rdd.foreach(println)
        }
      }
        
    
    
/*
      val words = lines.flatMap(line => line.split(" "))
      
      val mapword = words.map(word => (word, 1))
      val redword = mapword.reduce((x, y) => x)
 //     val dynamicEvent = new DynamicEvent()
 //     dynamicEvent.testLoadRuleDynamically()
 */
   /*
      lines.foreachRDD { rdd => 
        rdd.foreach(println)
      }
      * 
      */
     // lines.saveAsTextFiles("D:/out.txt")
    
 /*     
    val lineCollect = lines.mapPartitions ( mapParti => {
      val ksession = RuleRunner.makeRuleKieBase()
      //System.out.println("length : " + mapParti.length)
      //mapParti.filter(RuleRunner.runRule(_, ksession))
      mapParti.filter(RuleRunner.runRule(_,ksession)) // Remove empty CallLogs
    })
 */   
   // lineCollect.print
    
  //  System.out.println("mapPartitions read")

    /*
    val ksession = RuleRunner.makeRuleKieBase()
    contactsContactLists.foreachRDD(rdd=> {
      rdd.foreach(line => RuleRunner.runRule(line, ksession) )
    })
    */
 //   System.out.println("Count : " + lines.count)

 // lineCollect.foreach { x => println("x : " + x) }
    
  

    ssc.start()
    ssc.awaitTermination()
  }
}