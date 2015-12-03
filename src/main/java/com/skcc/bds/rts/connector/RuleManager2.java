package com.skcc.bds.rts.connector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.kie.api.KieBase;
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
import org.kie.api.definition.type.FactType;
import org.kie.internal.builder.conf.RuleEngineOption;



public class RuleManager2 {
	
	public static RuleEngineOption phreak = RuleEngineOption.PHREAK;

	
	public static ReleaseId releaseId1;
	public static ReleaseId releaseId2;
	public static KieServices ks = KieServices.Factory.get();
	public static KieModule km;
	public static KieContainer kc;
	public static KieSession ksession;
	//public static KieSession ksession;
	
	public static KieSession getRuleSession(String drlPath) throws IOException {
		String drlContentStrings = readFile(drlPath);
		
		
		releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
		km = createAndDeployJarInStreamMode(ks, releaseId1, drlContentStrings);
		kc = ks.newKieContainer(km.getReleaseId());
		ksession = kc.newKieSession();
 
        return ksession;
    }
	
//	public void insertFact(String log){
//		ksession.insert(1.0);
//	    ksession.fireAllRules();
//	}
//	public static void insertFact(String log, KieSession ksession1){
//		ksession1.insert(1.0);
//	    ksession1.fireAllRules();
//	}
	
	public static void insertFact(String log){
		ksession.insert(1.0);
	    ksession.fireAllRules();
	}
	
	public static void insertFact(String key, String value) throws InstantiationException, IllegalAccessException{
		FactType recordType = ksession.getKieBase().getFactType("defaultpkg","Record");
		Object record = recordType.newInstance();
		recordType.set(record,  "name",value);
		recordType.set(record,  "age",4);
		ksession.insert(record);
		ksession.fireAllRules();
//	    ksession1.fireAllRules();
	}
	
	public static KieSession reloadRuleSession(String  drlPath) throws IOException {
		String drlContentStrings = readFile(drlPath);
		System.out.println("Start reloadRuleSession");
		
//		for ( Object o : ksession.getObjects() ) {
//			ksession.delete( ksession.getFactHandle( o ) );
//	    }
		
		ksession.dispose();
		System.out.println("After dispose()");
		releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "2.0.0");
		km = createAndDeployJarInStreamMode(ks, releaseId2, drlContentStrings);
		System.out.println("After createAndDeployJarInStreamMode()");
		kc = ks.newKieContainer(km.getReleaseId());
		System.out.println("After newKieContainer()");
        ksession = kc.newKieSession();
        System.out.println("After newKieSession()");
        Results results = kc.updateToVersion(releaseId2);
        System.out.println("After updateToVersion()");
        
        return ksession;
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
	
	public static String readFile(String filePath) throws IOException {
		FileInputStream input = new FileInputStream(filePath);

		byte[] fileData = new byte[input.available()];

		input.read(fileData);
		input.close();

		return new String(fileData, "UTF-8");
	}
}