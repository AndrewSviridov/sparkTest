package com.skcc.bds.drools.test;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static com.skcc.bds.drools.test.IncrementalCompilationTest.createAndDeployJar;

public class KieContainerTest {

    @Test
    public void testSharedTypeDeclarationsUsingClassLoader() throws Exception {
        String type = "package org.drools.test\n" +
                      "declare Message\n" +
                      "   message : String\n" +
                      "end\n";

        String drl1 = "package org.drools.test\n" +
                      "rule R1 when\n" +
                      "   $o : Object()\n" +
                      "then\n" +
                      "   if ($o.getClass().getName().equals(\"org.drools.test.Message\") && $o.getClass() != new Message(\"Test\").getClass()) {\n" +
                      "       throw new RuntimeException();\n" +
                      "   }\n" +
                      "end\n";

        String drl2 = "package org.drools.test\n" +
                      "rule R2_2 when\n" +
                      "   $m : Message( message == \"Hello World\" )\n" +
                      "then\n" +
                      "   if ($m.getClass() != new Message(\"Test\").getClass()) {\n" +
                      "       throw new RuntimeException();\n" +
                      "   }\n" +
                      "end\n";

        KieServices ks = KieServices.Factory.get();
        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-delete", "1.0.0");
       // KieModule km = createAndDeployJar( ks, releaseId1, type, drl1, drl2 );

        KieContainer kieContainer = ks.newKieContainer(releaseId1);
        KieContainer kieContainer2 = ks.newKieContainer(releaseId1);

        KieSession ksession = kieContainer.newKieSession();
        KieSession ksession2 = kieContainer2.newKieSession();

        Class cls1 = kieContainer.getClassLoader().loadClass( "org.drools.test.Message");
        Constructor constructor = cls1.getConstructor(String.class);
        ksession.insert(constructor.newInstance("Hello World"));
        assertEquals( 2, ksession.fireAllRules() );

        Class cls2 = kieContainer2.getClassLoader().loadClass( "org.drools.test.Message");
        Constructor constructor2 = cls2.getConstructor(String.class);
        ksession2.insert(constructor2.newInstance("Hello World"));
        assertEquals( 2, ksession2.fireAllRules() );

        assertNotSame(cls1, cls2);
    }

    @Test
    public void testSharedTypeDeclarationsUsingFactTypes() throws Exception {
        String type = "package org.drools.test\n" +
                      "declare Message\n" +
                      "   message : String\n" +
                      "end\n";

        String drl1 = "package org.drools.test\n" +
                      "rule R1 when\n" +
                      "   $m : Message()\n" +
                      "then\n" +
                      "   if ($m.getClass() != new Message(\"Test\").getClass()) {\n" +
                      "       throw new RuntimeException();\n" +
                      "   }\n" +
                      "   System.out.println('Test');" +
                      "end\n";

        String drl2 = "package org.drools.test\n" +
                      "rule R2_2 when\n" +
                      "   $m : Message( message == \"Hello World\" )\n" +
                      "then\n" +
                      "   if ($m.getClass() != new Message(\"Test\").getClass()) {\n" +
                      "       throw new RuntimeException();\n" +
                      "   }\n" +
                      "   System.out.println('Hello World');" +
                      "end\n";

        KieServices ks = KieServices.Factory.get();
        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-delete", "1.0.0");
 //       createAndDeployJar( ks, releaseId1, type, drl1, drl2 );
        
        byte[] jar = createKJar( ks, releaseId1, null, type, drl1 );
        deployJar( ks, jar );
        

        KieContainer kieContainer = ks.newKieContainer(releaseId1);
        KieContainer kieContainer2 = ks.newKieContainer(releaseId1);

        KieSession ksession = kieContainer.newKieSession();
        KieSession ksession2 = kieContainer2.newKieSession();

        insertMessageFromTypeDeclaration_org( ksession );
        assertEquals( 2, ksession.fireAllRules() );

        ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-delete", "1.0.1");
        createAndDeployJar( ks, releaseId2, type, null, drl2 );

        System.out.println("Before Update Version");
        kieContainer.updateToVersion(releaseId2);

        // test with the old ksession ...
        System.out.println("// test with the old ksession ...");
        ksession = kieContainer.newKieSession();
        insertMessageFromTypeDeclaration_org( ksession );
        assertEquals( 1, ksession.fireAllRules() );

       
//        ksession = kieContainer.newKieSession();
//        insertMessageFromTypeDeclaration (ksession );
//        assertEquals( 1, ksession.fireAllRules() );

        // ... and with a brand new one
        System.out.println("// ... and with a brand new one");
        insertMessageFromTypeDeclaration_org( ksession2 );
        assertEquals( 2, ksession2.fireAllRules() );

     // check that the second kieContainer hasn't been affected by the update of the first one
        System.out.println("// check that the second kieContainer hasn't been affected by the update of the first one");
        ksession2 = kieContainer2.newKieSession();
        insertMessageFromTypeDeclaration_org( ksession2 );
        assertEquals( 2, ksession2.fireAllRules() );
    }
    
    @Test
    public void testSimple() throws Exception {
    	
	String type = "package org.drools.compiler.test1\n" +
            "declare RMessageR\n" +
            "   message1 : String\n" +
            "   log : String\n" +
            "   id : String\n" +
            "end\n";
	
	String type2 = "package org.drools.compiler.test1\n" +
            "declare SensorLog\n" +
            "   log : String\n" +
            "end\n";
//	+
//            " " + 
//            "declare Sensor\n" +
//            "   message1 : String\n" +
//            "   log : String\n" +
//            "end\n";
	

	  String drl1 = "package org.drools.compiler.test1\n" +
			  		"dialect 'mvel'" +
	                "rule R1 when\n" +
	                "   $m : RMessageR()\n" +
	                "then\n" +
	                "   System.out.println($m.message1);" +
	                "   System.out.println($m.log);" +
	                "end\n";
	  
	  String drl11 = "package org.drools.compiler.test1\n" +
		  		"dialect 'mvel'" +
              "rule R1 when\n" +
              "   $m : RMessageR()\n" +
              "then\n" +
              "   System.out.println('Update Rule ' + $m.message1);" +
              "   System.out.println('Update Rule ' + $m.log);" +
              "end\n";
	
	  String drl2 = "package org.drools.compiler.test1\n" +
	                "rule R2_2 when\n" +
	                "   $m : S( message1 == \"Hello World\" )\n" +
	                "then\n" +
	                "   if (m.message1.length == 0) {\n" +
	                "       throw new RuntimeException();\n" +
	                "   }\n" +
	                "   System.out.println('Hello World');" +
	                "end\n";
	
	  KieServices ks = KieServices.Factory.get();
	  // Create an in-memory jar for version 1.0.0
	  ReleaseId releaseId1 = ks.newReleaseId("com.skdc", "test-delete", "1.0.0");
	 
	  try
      {
		  byte[] jar = createKJar( ks, releaseId1, null, type, type2, drl1 );
		  deployJar( ks, jar );
      }
	  catch(Exception e)
      {
      	e.printStackTrace();       
      }


	  
	
	  KieContainer kieContainer = ks.newKieContainer(releaseId1);
	  
	  kieContainer.updateToVersion(releaseId1);
	
	  KieSession ksession = kieContainer.newKieSession();
	
	  insertMessageFromTypeDeclaration_new( ksession );
	  ksession.fireAllRules();
	
//	  for ( Object o : ksession.getObjects() ) {
//		  ksession.retract( ksession.getFactHandle( o ) );
//      }
	  
    }
    
    @Test
    public void testCep() throws Exception {
        

    	KieServices ks = KieServices.Factory.get();
    	KieFileSystem kfs = ks.newKieFileSystem();

        int i;
        BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream("D:/00.Source/600.Scala/testScala2/src/main/resources/ETL-rule1.drl")));
        String line = null;
        StringBuilder sbuilder = new StringBuilder();

        String str = br.readLine();
        while (str != null) {
        	sbuilder.append(str);
        	str = br.readLine();
        }
        System.out.println("Rule : " + sbuilder.toString());

     // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-delete", "1.0.0");
 //     createAndDeployJar( ks, releaseId1, type, drl1, drl2 );
        
//        byte[] jar = createKJar( ks, releaseId1, null, type, drl1, drl2 );
        byte[] jar = createKJar( ks, releaseId1, null, sbuilder.toString());
        deployJar( ks, jar );
        

        KieContainer kieContainer = ks.newKieContainer(releaseId1);
        
        //KieContainer kieContainer2 = ks.newKieContainer(releaseId1);

        //kieContainer.updateToVersion(releaseId1);
        KieSession ksession = kieContainer.newKieSession();
        //KieSession ksession2 = kieContainer2.newKieSession();

        try
        {
       insertMessageFromTypeDeclaration( ksession );
        }catch(Exception e)
        {
        	e.printStackTrace();       
        }
    }
    
    private void insertMessageFromTypeDeclaration_org(KieSession ksession) throws InstantiationException, IllegalAccessException {
        FactType messageType = ksession.getKieBase().getFactType("org.drools.test", "Message");
        Object message = messageType.newInstance();
        messageType.set(message, "message", "Hello World");
        ksession.insert(message);
    }
    

    private void insertMessageFromTypeDeclaration_new(KieSession ksession) throws InstantiationException, IllegalAccessException {
        FactType messageType = ksession.getKieBase().getFactType("org.drools.compiler.test1", "RMessageR");
        Object message = messageType.newInstance();
        messageType.set(message, "message1", "Field message1");
        messageType.set(message, "log", "Field log");
        ksession.insert(message);
        
//        FactType messageType2 = ksession.getKieBase().getFactType("org.drools.test", "Sensor");
//        Object message2 = messageType2.newInstance();
//        messageType2.set(message2, "log", "Sensor Data");
//        ksession.insert(message2);
    }
    
    private void insertSensoreFromTypeDeclaration(KieSession ksession) throws InstantiationException, IllegalAccessException {
        FactType messageType = ksession.getKieBase().getFactType("org.drools.compiler.test1", "SensorLog");
        Object message = messageType.newInstance();
        messageType.set(message, "log", "[2015-01-28 10:53:10.627][INFO ][TLOG] - send message[subject=_LOCAL.M10.REAL.EAP.dsE03S] : CMDFROMDCONDRESULT HDR=(dsE03S,TDS,ETSM44) TIME_STAMP=2015/01/28-10:53:10.6 EQPID=ETSM44 COMMAND=SETSENSORS RESULT=OK COMMENT=");
        ksession.insert(message);
        
//        FactType messageType2 = ksession.getKieBase().getFactType("org.drools.test", "Sensor");
//        Object message2 = messageType2.newInstance();
//        messageType2.set(message2, "log", "Sensor Data");
//        ksession.insert(message2);
    }
    
    public static byte[] createKJar(KieServices ks,
            ReleaseId releaseId,
            String pom,
            String... drls) {
		KieFileSystem kfs = ks.newKieFileSystem();
		if( pom != null ) {
			kfs.write("pom.xml", pom);
		} else {
			kfs.generateAndWritePomXML(releaseId);
		}
		for (int i = 0; i < drls.length; i++) {
			if (drls[i] != null) {
				kfs.write("src/main/resources/r" + i + ".drl", drls[i]);
			}
		}
		KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
		if( kb.getResults().hasMessages( org.kie.api.builder.Message.Level.ERROR ) ) {
			for( org.kie.api.builder.Message result : kb.getResults().getMessages() ) {
				System.out.println(result.getText());
			}
			return null;
		}
		InternalKieModule kieModule = (InternalKieModule) ks.getRepository()
		.getKieModule(releaseId);
		byte[] jar = kieModule.getBytes();
		return jar;
    }
    
    public static KieModule deployJar(KieServices ks, byte[] jar) {
        // Deploy jar into the repository
        Resource jarRes = ks.getResources().newByteArrayResource(jar);
        KieModule km = ks.getRepository().addKieModule(jarRes);
        
        return km;
    }

    @Test(timeout = 10000)
    public void testIncrementalCompilationSynchronization() throws Exception {
        final KieServices kieServices = KieServices.Factory.get();

        ReleaseId releaseId = kieServices.newReleaseId("org.kie.test", "sync-scanner-test", "1.0.0");
        createAndDeployJar( kieServices, releaseId, createDRL("rule0") );

        final KieContainer kieContainer = kieServices.newKieContainer(releaseId);

        KieSession kieSession = kieContainer.newKieSession();
        List<String> list = new ArrayList<String>();
        kieSession.setGlobal("list", list);
        kieSession.fireAllRules();
        kieSession.dispose();
        assertEquals(1, list.size());

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i < 10; i++) {
                    ReleaseId releaseId = kieServices.newReleaseId("org.kie.test", "sync-scanner-test", "1.0." + i);
                    createAndDeployJar( kieServices, releaseId, createDRL("rule" + i) );
                    kieContainer.updateToVersion(releaseId);
                }
            }
        });

        t.setDaemon(true);
        t.start();

        while (true) {
            kieSession = kieContainer.newKieSession();
            list = new ArrayList<String>();
            kieSession.setGlobal("list", list);
            kieSession.fireAllRules();
            kieSession.dispose();
            assertEquals(1, list.size());
            if (list.get(0).equals("rule9")) {
                break;
            }
        }
    }

    private String createDRL(String ruleName) {
        return "package org.kie.test\n" +
               "global java.util.List list\n" +
               "rule " + ruleName + "\n" +
               "when\n" +
               "then\n" +
               "list.add( drools.getRule().getName() );\n" +
               "end\n";
    }
    
    @Test
    public void testSharedTypeDeclarationsUsingFactTypes_org() throws Exception {
        String type = "package org.drools.test\n" +
                      "declare Message\n" +
                      "   message : String\n" +
                      "end\n";

        String drl1 = "package org.drools.test\n" +
                      "rule R1 when\n" +
                      "   $m : Message()\n" +
                      "then\n" +
                      "   if ($m.getClass() != new Message(\"Test\").getClass()) {\n" +
                      "       throw new RuntimeException();\n" +
                      "   }\n" +
                      "end\n";

        String drl2 = "package org.drools.test\n" +
                      "rule R2_2 when\n" +
                      "   $m : Message( message == \"Hello World\" )\n" +
                      "then\n" +
                      "   if ($m.getClass() != new Message(\"Test\").getClass()) {\n" +
                      "       throw new RuntimeException();\n" +
                      "   }\n" +
                      "end\n";

        KieServices ks = KieServices.Factory.get();
        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-delete", "1.0.0");
        createAndDeployJar( ks, releaseId1, type, drl1, drl2 );

        KieContainer kieContainer = ks.newKieContainer(releaseId1);
        KieContainer kieContainer2 = ks.newKieContainer(releaseId1);

        KieSession ksession = kieContainer.newKieSession();
        KieSession ksession2 = kieContainer2.newKieSession();

        insertMessageFromTypeDeclaration( ksession );
        assertEquals( 2, ksession.fireAllRules() );

        ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-delete", "1.0.0");
        createAndDeployJar( ks, releaseId2, type, null, drl2 );

        kieContainer.updateToVersion(releaseId2);

        // test with the old ksession ...
        ksession = kieContainer.newKieSession();
        insertMessageFromTypeDeclaration( ksession );
        assertEquals( 1, ksession.fireAllRules() );

        // ... and with a brand new one
        ksession = kieContainer.newKieSession();
        insertMessageFromTypeDeclaration (ksession );
        assertEquals( 1, ksession.fireAllRules() );

        // check that the second kieContainer hasn't been affected by the update of the first one
        insertMessageFromTypeDeclaration( ksession2 );
        assertEquals( 2, ksession2.fireAllRules() );

        ksession2 = kieContainer2.newKieSession();
        insertMessageFromTypeDeclaration( ksession2 );
        assertEquals( 2, ksession2.fireAllRules() );
    }

    private void insertMessageFromTypeDeclaration(KieSession ksession) throws InstantiationException, IllegalAccessException {
        FactType messageType = ksession.getKieBase().getFactType("org.drools.test", "Message");
        Object message = messageType.newInstance();
        messageType.set(message, "message", "Hello World");
        ksession.insert(message);
    }
}
