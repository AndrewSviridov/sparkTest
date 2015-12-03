package com.skcc.bds.drools.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.common.EventFactHandle;
import org.drools.core.definitions.impl.KnowledgePackageImpl;
import org.drools.core.rule.TypeDeclaration;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.definition.type.Annotation;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.definition.type.Role;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;


import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.KnowledgeBuilderResults;
import org.kie.internal.builder.ResultSeverity;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.utils.KieHelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Calendar;


public class TypeDeclarationTest {

	@Test
    public void testCrossPackageDeclares() {
        String pkg1 =
                "package org.drools.compiler.test1; " +
                "import org.drools.compiler.test2.GrandChild; " +
                "import org.drools.compiler.test2.Child; " +
                "import org.drools.compiler.test2.BarFuu; " +

                "declare FuBaz foo : String end " +

                "declare Parent " +
                "   unknown : BarFuu " +
                "end " +

                "declare GreatChild extends GrandChild " +
                "   father : Child " +
                "end "
                ;

        String pkg2 =
                "package org.drools.compiler.test2; " +
                "import org.drools.compiler.test1.Parent; " +
                "import org.drools.compiler.test1.FuBaz; " +

                "declare BarFuu " +
                "   baz : FuBaz " +
                "end " +

                "declare Child extends Parent " +
                "end " +

                "declare GrandChild extends Child " +
                "   notknown : FuBaz " +
                "end " 
                ;

        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.generateAndWritePomXML( ks.newReleaseId( "test", "foo", "1.0" ) );
        KieModuleModel km = ks.newKieModuleModel();
        km.newKieBaseModel( "rules" )
                .addPackage( "org.drools.compiler.test2" )
                .addPackage( "org.drools.compiler.test1" );
        kfs.writeKModuleXML( km.toXML() );

        KieResources kr = ks.getResources();
        Resource r1 = kr.newByteArrayResource( pkg1.getBytes() )
                .setResourceType( ResourceType.DRL )
                .setSourcePath( "org/drools/compiler/test1/p1.drl" );
        Resource r2 = kr.newByteArrayResource( pkg2.getBytes() )
                .setResourceType( ResourceType.DRL )
                .setSourcePath( "org/drools/compiler/test2/p2.drl" );

        kfs.write( r1 );
        kfs.write( r2 );

        KieBuilder builder = ks.newKieBuilder( kfs );
        builder.buildAll();

        assertEquals( Collections.emptyList(), builder.getResults().getMessages( Message.Level.ERROR ) );

        KieContainer kc = ks.newKieContainer(builder.getKieModule().getReleaseId());
        FactType ft = kc.getKieBase( "rules" ).getFactType( "org.drools.compiler.test2", "Child" );

        assertNotNull( ft );
        assertNotNull( ft.getFactClass() );
        assertEquals( "org.drools.compiler.test1.Parent", ft.getFactClass().getSuperclass().getName() );
        
//        KieSession ksession = kc.newKieSession();
//        ksession.insert("test");
//        ksession.fireAllRules();
    }

	
	
	@Test
    public void testMultipleAnnotationDeclarations() {
        String str1 = "";
        str1 += "package org.kie1 " +
                "" +
               "declare Foo \n" +
               "    name : String " +
               "    age : int " +
               "end ";

        String str2 = "";
        str2 += "package org.kie2 " +
               "" +
               "declare org.kie1.Foo " +
               "    @role(event) " +
               "end ";

        String str3 = "";
        str3 += "package org.kie3 " +
               "" +
               "declare org.kie1.Foo " +
               "    @propertyReactive " +
               "end ";

        String str4 = "" +
                      "package org.kie4; " +
                      "import org.kie1.Foo; " +
                      "" +
                      "rule Check " +
                      "when " +
                      " $f : Foo( name == 'bar' ) " +
                      "then " +
                      " modify( $f ) { setAge( 99 ); } " +
                      " System.out.println('test');" + 
                      "end ";

        KieHelper helper = new KieHelper();
        helper.addContent( str1, ResourceType.DRL );
        helper.addContent( str2, ResourceType.DRL );
        helper.addContent( str3, ResourceType.DRL );
        helper.addContent( str4, ResourceType.DRL );

        List<Message> msg = helper.verify().getMessages( Message.Level.ERROR );
        System.out.println( msg );
        assertEquals( 0, msg.size() );

        KieBase kieBase = helper.build();
        FactType type = kieBase.getFactType( "org.kie1", "Foo" );
        assertEquals( 2, type.getFields().size() );

        Object foo = null;
        try {
            foo = type.newInstance();
            type.set( foo, "name", "bar" );
            assertEquals( "bar", type.get( foo, "name" ) );
        } catch ( InstantiationException e ) {
            fail( e.getMessage() );
        } catch ( IllegalAccessException e ) {
            fail( e.getMessage() );
        }

        KieSession session = kieBase.newKieSession();
        FactHandle handle = session.insert( foo );
        int n = session.fireAllRules( 5 );

        assertTrue( handle instanceof EventFactHandle );
        assertEquals( 1, n );
        assertEquals( 99, type.get( foo, "age" ) );
    }


}
