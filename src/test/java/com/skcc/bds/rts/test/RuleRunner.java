package com.skcc.bds.rts.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.conf.RuleEngineOption;
import org.kie.internal.io.ResourceFactory;





public class RuleRunner implements Serializable{
	public static RuleEngineOption phreak = RuleEngineOption.PHREAK;
	
	public static KieSession ksession;
	
	public static void createKieSession() throws Exception {
		//System.out.println("LOG : " + log);
		
		String str = "global java.util.Map map;\n" +
                " \n" +
                "declare Double\n" +
                "@role(event)\n" +
                "end\n" +
                " \n" +
                "declare window Streem\n" +
                //"    Double() over window:length( 5 )\n" +
                "    Double() over window:time( 1s )\n" +
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

       ksession = getKieSessionFromContentStrings(str);

        Map res = new HashMap();
        ksession.setGlobal( "map", res );

	    ksession.insert(1.0);
	    ksession.fireAllRules();
	}
	
	public static boolean testAccumulateWithWindowWithoutKieSession(String log) throws Exception {
		//System.out.println("LOG : " + log)

	    ksession.insert(1.0);
	    ksession.fireAllRules();
	    Thread.sleep(50);
		
		return true;
	}
	
	public static boolean testAccumulateWithWindow(String log) throws Exception {
		//System.out.println("LOG : " + log);
		
		String str = "global java.util.Map map;\n" +
                " \n" +
                "declare Double\n" +
                "@role(event)\n" +
                "end\n" +
                " \n" +
                "declare window Streem\n" +
                "    Double() over window:length( 5 )\n" +
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

        KieSession ksession = getKieSessionFromContentStrings(str);

        Map res = new HashMap();
        ksession.setGlobal( "map", res );
        ksession.fireAllRules();

	    ksession.insert(1.0);
	    ksession.fireAllRules();
        
//        for ( int j = 0; j < 33; j++ ) {
//            ksession.insert(1.0);
//            ksession.fireAllRules();
//        }
		
		
		return true;
	}
	
	public static KieSession getKieBaseFromContentStrings(String... drlContentStrings) {
        KieBase kbase = loadKnowledgeBaseFromString(null, null, phreak,
                drlContentStrings);
        
        return kbase.newKieSession();
    }
	
	
	protected static KieSession getKieSessionFromContentStrings(String... drlContentStrings) {
        KieBase kbase = loadKnowledgeBaseFromString(null, null, phreak,
                drlContentStrings);
        return kbase.newKieSession();
    }
	
	protected static KnowledgeBase loadKnowledgeBaseFromString( KnowledgeBuilderConfiguration config, KieBaseConfiguration kBaseConfig, RuleEngineOption phreak, String... drlContentStrings) {
		KnowledgeBuilder kbuilder = config == null ? KnowledgeBuilderFactory.newKnowledgeBuilder() : KnowledgeBuilderFactory.newKnowledgeBuilder(config);
		for (String drlContentString : drlContentStrings) {
			kbuilder.add(ResourceFactory.newByteArrayResource(drlContentString
					.getBytes()), ResourceType.DRL);
		}

		if (kbuilder.hasErrors()) {
			kbuilder.getErrors().toString();
		}
		if (kBaseConfig == null) {
			kBaseConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
		}
		kBaseConfig.setOption(phreak);
		KnowledgeBase kbase = kBaseConfig == null ? KnowledgeBaseFactory.newKnowledgeBase() : KnowledgeBaseFactory.newKnowledgeBase(kBaseConfig);
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		return kbase;
	}
}
