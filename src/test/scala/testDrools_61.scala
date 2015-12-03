

import org.drools.io.ResourceFactory
import org.drools.builder.KnowledgeBuilderFactory
import org.drools.builder.ResourceType
import collection.JavaConversions._
import org.slf4j.LoggerFactory


object testDrools_61 extends App {

  val logger = LoggerFactory.getLogger(getClass())
  testDrool_ETL()
  
  def testDrool_ETL(): Unit = {
    System.setProperty("drools.dialect.java.compiler", "JANINO")
    logger.info("tetst")
    val config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration()
    config.setProperty("drools.dialect.mvel.strict", "false")
    val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(config)

    val res = ResourceFactory.newClassPathResource("ETL-rule.drl")
    kbuilder.add(res, ResourceType.DRL)

    val errors = kbuilder.getErrors();
    if (errors.size() > 0) {
      for (error <- errors) logger.error("ERROR : " + error.getMessage())
      throw new IllegalArgumentException("Problem with the Knowledge base");
    }


    val kbase = kbuilder.newKnowledgeBase()
    
    val session = kbase.newStatefulKnowledgeSession()
    session.setGlobal("logger", LoggerFactory.getLogger("ETL-rule.drl"))

    session.insert(new Sensor(log="  Equipment ID   =ETSM44"))
    session.fireAllRules()
  }
}