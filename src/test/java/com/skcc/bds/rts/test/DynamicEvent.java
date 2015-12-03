package com.skcc.bds.rts.test;

import org.junit.Test;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;


public class DynamicEvent {

	@Test
    public void loadRuleDynamically() throws Exception {
       
        String str1 = "global java.util.Map map;\n" +
                " \n" +
                "declare Double\n" +
                "@role(event)\n" +
                "end\n" +
                " \n" +
                "declare window Streem\n" +
                "    Double() over window:length( 11 ) from entry-point data\n" +
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
                "    System.out.println( \"We have a sum0 \" + $a );\n" +
                "end\n";
        
        String str2 = "global java.util.Map map;\n" +
                " \n" +
                "declare Double\n" +
                "@role(event)\n" +
                "end\n" +
                " \n" +
                "declare window Streem2\n" +
                "    Double() over window:length( 7 ) from entry-point data\n" +
                "end\n" +
                " \n" +
                "rule \"See2\"\n" +
                "when\n" +
                "    $a : Double() from accumulate (\n" +
                "        $d: Double()\n" +
                "            from window Streem2,\n" +
                "        sum( $d )\n" +
                "    )\n" +
                "then\n" +
                "    System.out.println( \"We have a sum1 \" + $a );\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();

        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieModule km = createAndDeployJarInStreamMode(ks, releaseId1, str1);

        // Create a session and fire rules
        KieContainer kc = ks.newKieContainer(km.getReleaseId());
        KieSession ksession = kc.newKieSession();
    
        
        for ( int j = 0; j < 33; j++ ) {
            ksession.getEntryPoint( "data" ).insert(1.0);
            ksession.fireAllRules();
        }
      
        ksession.dispose();
        System.out.println("A Count : " + ksession.getObjects().size());

        for ( Object o : ksession.getObjects() ) {
		//	ksession.delete( ksession.getFactHandle( o ) );
        }
        System.out.println("B Count : " + ksession.getObjects().size());
        
        // Create a new jar for version 1.1.0
        ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        km = createAndDeployJarInStreamMode(ks, releaseId1, str2);
        kc = ks.newKieContainer(releaseId1);
        ksession = kc.newKieSession();

        // try to update the container to version 1.1.0
        Results results = kc.updateToVersion(releaseId1);

        for ( int j = 0; j < 12; j++ ) {
            ksession.getEntryPoint( "data" ).insert(1.0);
            ksession.fireAllRules();
        }
        

    }

    public static KieModule createAndDeployJarInStreamMode(KieServices ks,
                                                           ReleaseId releaseId,
                                                           String... drls) {
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.generateAndWritePomXML(releaseId);
        KieModuleModel module = ks.newKieModuleModel();

        KieBaseModel defaultBase = module.newKieBaseModel("kBase1");
        defaultBase.setEventProcessingMode(EventProcessingOption.STREAM).setDefault(true);
        defaultBase.newKieSessionModel("defaultKSession").setDefault(true);
        
        kfs.writeKModuleXML(module.toXML());

        for (int i = 0; i < drls.length; i++) {
            kfs.write("src/main/resources/rules" + i + ".drl", drls[i]);
        }

        KieBuilder kb = ks.newKieBuilder(kfs);
        kb.buildAll();
        if (kb.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            System.out.println(kb.getResults().toString());
        }
        return kb.getKieModule();
    }
}


