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
import org.kie.internal.builder.conf.RuleEngineOption;

public class RuleManager {
	
	public static RuleEngineOption phreak = RuleEngineOption.PHREAK;

	public static KieServices ks = KieServices.Factory.get();
	public static ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
	
	public static KieSession getRuleSession(String... drlContentStrings) {
		KieModule km = createAndDeployJarInStreamMode(ks, releaseId1, drlContentStrings);
		KieContainer kc = ks.newKieContainer(km.getReleaseId());
        KieSession ksession = kc.newKieSession();
 
        return ksession;
    }
	
	public static KieSession reloadRuleSession(String... drlContentStrings) {
		KieModule km = createAndDeployJarInStreamMode(ks, releaseId1, drlContentStrings);
		KieContainer kc = ks.newKieContainer(km.getReleaseId());
        KieSession ksession1 = kc.newKieSession();
        Results results = kc.updateToVersion(releaseId1);
        
        return ksession1;
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
	
	public String reaFile(String filePath) throws IOException {
		FileInputStream input = new FileInputStream(filePath);

		byte[] fileData = new byte[input.available()];

		input.read(fileData);
		input.close();

		return new String(fileData, "UTF-8");
	}
}
