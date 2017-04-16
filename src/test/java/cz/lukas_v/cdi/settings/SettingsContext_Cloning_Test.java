package cz.lukas_v.cdi.settings;

import java.util.Collections;

import javax.enterprise.inject.spi.Bean;

import org.junit.Test;

import cz.lukas_v.cdi.settings.FileSettingsLoader;
import cz.lukas_v.cdi.settings.SettingsContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SettingsContext_Cloning_Test {
	
	private SettingsContext context;
	
	@Test
	public void cloneableObject_returnedInstancesAreNotTheSame() throws Exception {
		context = new SettingsContext
		(
			"FileSettings.properties", 
			Collections.singleton(DummySettings_Cloneable.class)
		);
		context.initialize();
		
		Bean<?> bean = mock(Bean.class);
		doReturn(DummySettings_Cloneable.class)
			.when(bean)
			.getBeanClass();
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "FirstDummySettings.txt");
		
		context.reloadSettings();
		
		Object first = context.get(bean, null);
		Object second = context.get(bean, null);
		
		assertNotNull(first);
		assertEquals(DummySettings_Cloneable.class, first.getClass());
		
		assertNotNull(second);
		assertEquals(DummySettings_Cloneable.class, second.getClass());
		
		assertNotSame(first, second);
	}
	
	@Test
	public void cloneableObject_cloneNotSupportedThrown_returnsInternalInstance() throws Exception {
		context = new SettingsContext
		(
			"FileSettings.properties", 
			Collections.singleton(DummySettings_FailingCloneable.class)
		);
		context.initialize();
		
		Bean<?> bean = mock(Bean.class);
		doReturn(DummySettings_FailingCloneable.class)
			.when(bean)
			.getBeanClass();
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "FirstDummySettings.txt");
		
		context.reloadSettings();
		
		DummySettings_FailingCloneable.failOnCloneNotSupported();
		
		Object first = context.get(bean, null);
		Object second = context.get(bean, null);
		
		assertNotNull(first);
		assertEquals(DummySettings_FailingCloneable.class, first.getClass());
		assertSame(first, second);
	}
	
	@Test
	public void cloneableObject_nullPointerThrown_returnsInternalInstance() throws Exception {
		context = new SettingsContext
		(
			"FileSettings.properties", 
			Collections.singleton(DummySettings_FailingCloneable.class)
		);
		context.initialize();
		
		Bean<?> bean = mock(Bean.class);
		doReturn(DummySettings_FailingCloneable.class)
			.when(bean)
			.getBeanClass();
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "FirstDummySettings.txt");
		
		context.reloadSettings();
		
		DummySettings_FailingCloneable.failOnNullPointerException();
		
		Object first = context.get(bean, null);
		Object second = context.get(bean, null);
		
		assertNotNull(first);
		assertEquals(DummySettings_FailingCloneable.class, first.getClass());
		assertSame(first, second);
	}
	
	@Test
	public void cloneableObject_nullReturned_returnsInternalInstance() throws Exception {
		context = new SettingsContext
		(
			"FileSettings.properties", 
			Collections.singleton(DummySettings_FailingCloneable.class)
		);
		context.initialize();
		
		Bean<?> bean = mock(Bean.class);
		doReturn(DummySettings_FailingCloneable.class)
			.when(bean)
			.getBeanClass();
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "FirstDummySettings.txt");
		
		context.reloadSettings();
		
		DummySettings_FailingCloneable.returnNull();
		
		Object first = context.get(bean, null);
		Object second = context.get(bean, null);
		
		assertNotNull(first);
		assertEquals(DummySettings_FailingCloneable.class, first.getClass());
		assertSame(first, second);
	}
	
	@Test
	public void cloneableObject_unexpectedClassReturned_returnsInternalInstance() throws Exception {
		context = new SettingsContext
		(
			"FileSettings.properties", 
			Collections.singleton(DummySettings_FailingCloneable.class)
		);
		context.initialize();
		
		Bean<?> bean = mock(Bean.class);
		doReturn(DummySettings_FailingCloneable.class)
			.when(bean)
			.getBeanClass();
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "FirstDummySettings.txt");
		
		context.reloadSettings();
		
		DummySettings_FailingCloneable.returnDirrefentClass();
		
		Object first = context.get(bean, null);
		Object second = context.get(bean, null);
		
		assertNotNull(first);
		assertEquals(DummySettings_FailingCloneable.class, first.getClass());
		assertSame(first, second);
	}
	
	@Test
	public void uncloneableObject_returnsInternalInstanceEachTime() throws Exception {
		context = new SettingsContext
		(
			"FileSettings.properties", 
			Collections.singleton(DummySettings_NotCloneable.class)
		);
		context.initialize();
		
		Bean<?> bean = mock(Bean.class);
		doReturn(DummySettings_NotCloneable.class)
			.when(bean)
			.getBeanClass();
		
		context
			.getSettings()
			.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, "FirstDummySettings.txt");
		
		context.reloadSettings();
		
		Object first = context.get(bean, null);
		Object second = context.get(bean, null);
		
		assertNotNull(first);
		assertEquals(DummySettings_NotCloneable.class, first.getClass());
		assertSame(first, second);
	}
	
}