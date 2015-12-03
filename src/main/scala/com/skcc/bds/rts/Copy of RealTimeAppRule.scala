package com.skcc.bds.rts

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.scheduler.{StreamingListenerBatchCompleted, StreamingListenerBatchStarted, StreamingListener}

import scala.collection.mutable.ArrayBuffer

import com.skcc.bds.rts.rule.RuleRunner
import com.skcc.bds.rts.connector.RedisPool

object RealTimeAppRule1 {

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
      .setMaster("local[2]")
           
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    ssc.checkpoint("src/main/resources/data/checkpoint/realtime")
      
    val redisPool = new RedisPool("127.0.0.1", 6379)
    val jedis = redisPool.getRedisPool()
    
    val isRuleExist = true;
 
    ssc.addStreamingListener(new StreamingListener() {
        override def onBatchStarted(batchStarted: StreamingListenerBatchStarted): Unit = {
          System.out.println("Batch Started")
          val isUpdate = redisPool.getList(jedis, "workflowid:isRuleUpdate")
          System.out.println("Redis Data : " + isUpdate.toBoolean)
          
        }
        override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
          System.out.println("Batch Completed\n")
        }
    })
 
    val topicMap = topics.split(",").map((_, 1)).toMap
    val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2).cache
    val counts = lines.map(word => (word, 1))
    
    //Check Event Component
    if(isRuleExist) {
      //Cluster Mode
      if(args(0) == "cluster") {
        System.out.println ("Running [CLUSTER] MODE")
          
        val ksession = RuleRunner.makeRuleKieBase(drl)
        lines.foreachRDD { rdd => 
          rdd.collect.foreach { x => RuleRunner.runRule(x,ksession) }
        }
        lines.count()
      }
      //Client Mode
      else {
        System.out.println ("Running [CLIENT] MODE")
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