package com.github.lukas_v.cdi.settings;

import java.util.Collections;

import javax.enterprise.inject.spi.Bean;

import org.junit.Before;
import org.junit.Test;

import com.github.lukas_v.cdi.settings.FileSettingsLoader;
import com.github.lukas_v.cdi.settings.SettingsContext;
import com.typesafe.config.ConfigException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SettingsContext_Loading_Test {
	
	private SettingsContext context;
	
	@Before
	public void beforeEachTest() throws Exception {
		context = new SettingsContext
		(
			"FileSettings.properties", 
			Collections.singleton(DummySettings_Cloneable.class)
		);
		context.initialize();
	}
	
	@Test
	public void loadInvalidSettings() throws Exception {
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "EmptyFile.txt");
		
		try {
			context.reloadSettings();
			
			assertTrue("Calling reloadSettings() must throw an exception.", false);
			
		} catch(Exception ex) {
			assertEquals(ConfigException.Missing.class, ex.getClass());
		}
		
		assertFalse(context.isActive());
	}
	
	@Test
	public void loadValidSettings() throws Exception {
		Bean<?> bean = mock(Bean.class);
		doReturn(DummySettings_Cloneable.class)
			.when(bean)
			.getBeanClass();
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "FirstDummySettings.txt");
		
		context.reloadSettings();
		
		assertTrue(context.isActive());
		
		Object ret = context.get(bean, null);
		
		assertNotNull(ret);
		assertEquals(DummySettings_Cloneable.class, ret.getClass());
		assertEquals("cloneable : first", ((DummySettings_Cloneable)ret).getValue());
	}
	
	@Test
	public void reloadValidSettings() throws Exception {
		Bean<?> bean = mock(Bean.class);
		doReturn(DummySettings_Cloneable.class)
			.when(bean)
			.getBeanClass();
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "FirstDummySettings.txt");
		
		context.reloadSettings();
		
		context.get(bean, null);
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "SecondDummySettings.txt");
		
		context.reloadSettings();
		
		Object ret = context.get(bean, null);
		
		assertNotNull(ret);
		assertEquals(DummySettings_Cloneable.class, ret.getClass());
		assertEquals("cloneable : second", ((DummySettings_Cloneable)ret).getValue());
	}
	
	@Test
	public void reloadInvalidSettings() throws Exception {
		Bean<?> bean = mock(Bean.class);
		doReturn(DummySettings_Cloneable.class)
			.when(bean)
			.getBeanClass();
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "FirstDummySettings.txt");
		
		context.reloadSettings();
		
		context.get(bean, null);
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "EmptyFile.txt");
		
		try {
			context.reloadSettings();
			
			assertTrue("Calling reloadSettings() must throw an exception.", false);
			
		} catch(Exception ex) {
			assertEquals(ConfigException.Missing.class, ex.getClass());
		}
		
		assertTrue(context.isActive());
		
		Object ret = context.get(bean, null);
		
		assertNotNull(ret);
		assertEquals(DummySettings_Cloneable.class, ret.getClass());
		assertEquals("cloneable : first", ((DummySettings_Cloneable)ret).getValue());
	}
	
}