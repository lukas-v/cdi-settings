package com.github.lukas_v.cdi.settings;

import java.io.File;
import java.util.Optional;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.lukas_v.cdi.settings.ReloadSettings;
import com.github.lukas_v.cdi.settings.Settings;

import static org.junit.Assert.*;

@RunWith(Suite.class)
@SuiteClasses
({
	SettingsContextExtension_Wildfly_IT.Isolated_Annotated.class, 
	SettingsContextExtension_Wildfly_IT.NotIsolated_Annotated.class, 
	SettingsContextExtension_Wildfly_IT.WithoutDeplyomentDescriptor_WithoutBeansFile.class
})
public class SettingsContextExtension_Wildfly_IT {
	
	private static abstract class WildflyTestBase {
		
		static EnterpriseArchive createDeployment(String deploymentStructute, String beans) {
			EnterpriseArchive ear = ShrinkWrap
				.create(EnterpriseArchive.class, "wildfly-10-1.ear");
			
			JavaArchive wrongLibrary = ShrinkWrap
				.create(JavaArchive.class, "wrong-library.jar").addAsManifestResource
				(
					new File("src/test/resources/beans_none.xml"), 
					"beans.xml"
				)
				.addClass(DummySettings_FailingCloneable.class);
			
			JavaArchive validLibrary = ShrinkWrap
				.create(JavaArchive.class, "library.jar")
				.addAsResource
				(
					new File("src/test/resources/FileSettings.properties"), 
					"settings.properties"
				)
				.addAsResource
				(
					new File("src/test/resources/FirstDummySettings.txt"), 
					"FirstDummySettings.txt"
				)
				.addClass(DummySettings_NotCloneable.class);
			
			WebArchive webApplication = ShrinkWrap
				.create(WebArchive.class, "arquillian.war")
				.addClass(DummySettings_Cloneable.class)
		        .addClass(SettingsContextExtension_Wildfly_IT.class);
			
			if(beans != null)
			{
				validLibrary.addAsManifestResource
				(
					new File(beans), 
					"beans.xml"
				);
				
				webApplication.addAsManifestResource
				(
					new File(beans), 
					"beans.xml"
				);
			}
			
			if(deploymentStructute != null)
			{
				ear.addAsManifestResource
				(
					new File(deploymentStructute), 
					"jboss-deployment-structure.xml"
				);
			}
			
			return ear.addAsLibraries
				(
					Maven.resolver()
						.loadPomFromFile("pom.xml")
						.importRuntimeDependencies()
						.resolve()
						.withTransitivity()
						.asFile()
				)
				.addAsLibraries
				(
					ShrinkWrap
						.create(ZipImporter.class, "cdi-settings.jar")
						.importFrom(new File("target/cdi-settings.jar"))
						.as(JavaArchive.class)
				)
				.addAsLibraries(validLibrary)
				.addAsLibraries(wrongLibrary)
				.addAsModule(webApplication);
	    }
		
		@Inject @Any Event<ReloadSettings> reloadEvent;
		
		@Inject DummySettings_Cloneable directInjection;
		@Inject DummySettings_NotCloneable injectionFromDependency;
		@Inject Instance<DummySettings_FailingCloneable> unknownDependency;
		
		
		
		@Test
		public void verifyValueOfDirectInjection() {
			assertNotNull(directInjection);
			assertEquals("cloneable : first", directInjection.getValue());
		}
		
		@Test
		public void verifyValueOfDirectLookup() {
			Optional<DummySettings_Cloneable> instance = Settings.snapshotOf(DummySettings_Cloneable.class);
			
			assertTrue(instance.isPresent());
			assertEquals("cloneable : first", instance.get().getValue());
		}
		
		
		
		@Test
		public void verifyValueOfInjectionFromDependency() {
			assertNotNull(injectionFromDependency);
			assertEquals("not cloneable : first", injectionFromDependency.getValue());
		}
		
		@Test
		public void verifyValueOfLookupFromDependency() {
			Optional<DummySettings_NotCloneable> instance = Settings.snapshotOf(DummySettings_NotCloneable.class);
			
			assertTrue(instance.isPresent());
			assertEquals("not cloneable : first", instance.get().getValue());
		}
		
		
		
		
		@Test
		public void verifyThatEmptyOprionalIsReturnedForUnknownClass() {
			if(!unknownDependency.isUnsatisfied()) {
				throw new IllegalStateException();
			}
			
			Optional<DummySettings_FailingCloneable> instance = Settings.snapshotOf(DummySettings_FailingCloneable.class);
			
			assertFalse(instance.isPresent());
		}
		
		@Test
		@Ignore
		public void verifyReload() {
			reloadEvent.fire(new ReloadSettings());
		}
		
	}
	
	@RunWith(Arquillian.class)
	public static class NotIsolated_Annotated extends WildflyTestBase {
		
		@Deployment
		public static EnterpriseArchive createDeployment() {
			return createDeployment
			(
				"src/test/resources/jboss-deployment-structure_not-isolated.xml", 
				"src/test/resources/beans_annotated.xml"
			);
		}
		
	}
	
	@RunWith(Arquillian.class)
	public static class Isolated_Annotated extends WildflyTestBase {
		
		@Deployment
		public static EnterpriseArchive createDeployment() {
			return createDeployment
			(
				"src/test/resources/jboss-deployment-structure_isolated.xml", 
				"src/test/resources/beans_all.xml"
			);
		}
		
	}
	
	@RunWith(Arquillian.class)
	public static class WithoutDeplyomentDescriptor_WithoutBeansFile extends WildflyTestBase {
		
		@Deployment
		public static EnterpriseArchive createDeployment() {
			return createDeployment(null, null);
		}
		
	}
	
}