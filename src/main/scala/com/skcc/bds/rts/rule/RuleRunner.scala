package com.skcc.bds.rts.rule

import org.kie.api.KieBase
import org.kie.api.runtime.KieSession
import com.skcc.bds.rts.connector.RuleManager
import org.apache.spark.streaming.dstream.DStream
import com.skcc.bds.rts.connector.RuleManager2

object RuleRunner {
    
  def makeRuleKieBase(drl:String):KieSession = {
  
    val str = "global java.util.Map map;\n" +
                " \n" +
                "declare Double\n" +
                "@role(event)\n" +
                "end\n" +
                " \n" +
                "declare window Streem\n" +
                "    Double() over window:length( 200 )\n" +
                "end\n" +
                " \n" +
                "rule \"See\"\n" +
                "when\n" +
                "    $a : Double() from accumulate (\n" +
                "        $d: Double()\n" +
                "            from window Streem,\n" +
                "        sum( $d )\n" +
                "    )\n" +
                "then\n" +
                "    System.out.println( \"We have a sum \" + $a );\n" +
                "end\n";

    val fileContents = scala.io.Source.fromFile(drl).getLines.mkString
    val ksession = RuleManager.getRuleSession(fileContents)
    println("Success Creating ksession")
    return ksession
  }
  
  def reloadRule(drl:String, ksession:KieSession):KieSession = {
    ksession.dispose()
    val fileContents = scala.io.Source.fromFile(drl).getLines.mkString
    val ksession1 = RuleManager.reloadRuleSession(fileContents)
    return ksession1
    
  }
  
  
//  def reloadRule(drl:String, ksession:KieSession):KieSession = {
//    ksession.dispose()
//    val fileContents = scala.io.Source.fromFile(drl).getLines.mkString
//    val ksession1 = RuleManager.getKieBaseFromContentStrings(fileContents)
//    
//    return ksession1
//    
//  }

  
  def runRule(log:String, ksession:KieSession):Boolean = {
    ksession.insert(1.0)
    ksession.fireAllRules()
    return true
  }
  
    def runRule(lines:DStream[String]):Boolean = {
        lines.foreachRDD { rdd => 
          rdd.collect.foreach { x => RuleManager2.insertFact("key", x) }
        }
        lines.count()
        lines.print()
    return true
  }
}