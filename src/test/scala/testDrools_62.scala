
//import dummy.model._

import org.kie.api.KieServices
import org.slf4j.LoggerFactory
import java.io.StringReader
import org.kie.api.io.ResourceType
import org.kie.internal.builder.InternalKieBuilder
import scala.collection.JavaConversions._

object testDrools_62 extends App {

  val logger = LoggerFactory.getLogger(getClass())

  testUpdateRule()
  
 
  def testUpdateRule(): Unit = {
    val fileContents1 = scala.io.Source.fromFile("src/main/resources/ETL-rule1.drl").getLines.mkString
    val fileContents2 = scala.io.Source.fromFile("src/main/resources/ETL-rule2.drl").getLines.mkString
    val list = new java.util.ArrayList[String]()
    
    
    var ks = KieServices.Factory.get();
    var id = ks.newReleaseId( "org.test", "foo", "1.0-SNAPSHOT" );
    
    var kfs2 = ks.newKieFileSystem();
    var kieBuilder2 = ks.newKieBuilder( kfs2 );
    kfs2.generateAndWritePomXML( id );
    kfs2.write( ks.getResources()
                        .newReaderResource( new StringReader( fileContents1 ) )
                        .setResourceType( ResourceType.DRL )
                        .setSourcePath( "drl1.drl" ) );

    kieBuilder2.buildAll();

    var kc2 = ks.newKieContainer( id );
    var ksession2 = kc2.newKieSession();
    ksession2.setGlobal( "list", list );

    ksession2.insert(new Sensor(log="  Equipment ID   =ETSM44"));
    ksession2.insert(new Sensor(log="  Equipment ID   =ETSM44"));
    ksession2.insert(new Sensor(log="  Equipment ID   =ETSM44"));
    ksession2.fireAllRules();

    for( ttt <- ksession2.getObjects() ){
          ksession2.retract( ksession2.getFactHandle( ttt ) )
        }
    
    
    kfs2.write( ks.getResources()
                        .newReaderResource( new StringReader( fileContents2 ) )
                        .setResourceType( ResourceType.DRL )
                        .setSourcePath( "drl1.drl" ) );

    kieBuilder2.asInstanceOf[InternalKieBuilder].incrementalBuild();
    

    var updateResults = kc2.updateToVersion( id );
    System.out.println("Update Rule")
    
    ksession2.insert(new Sensor(log="  Equipment ID   =ETSM44"));
    ksession2.fireAllRules();
  }
}