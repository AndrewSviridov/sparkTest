package com.skcc

import org.kie.api.KieServices
import java.io.StringReader
import org.kie.api.io.ResourceType
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

object testCep extends App {

  main()
  
  def main(): Unit = {
    
    System.out.println("Tets");
    
    val list = new java.util.ArrayList[String]()
      val fileContents = scala.io.Source.fromFile("D:/00.Source/600.Scala/realtime/src/main/resources/ETL-rule.drl").getLines.mkString
  
      var ks   = KieServices.Factory.get();
      var id   = ks.newReleaseId( "com.skcc", "cep", "1.0-SNAPSHOT" );   
      var kfs = ks.newKieFileSystem();
      var kieBuilder = ks.newKieBuilder( kfs );
      
      kfs.generateAndWritePomXML( id );
      kfs.write( ks.getResources()
                          .newReaderResource( new StringReader( fileContents ) )
                          .setResourceType( ResourceType.DRL )
                          .setSourcePath( "cepRule.drl" ) );
  
      kieBuilder.buildAll();
      
      var kc = ks.newKieContainer( id );
      var ksession = kc.newKieSession(); 
      
      ksession.insert("test")
     
      ksession.fireAllRules()
  }
}