package com.github.lukas_v.cdi.settings;

import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.lukas_v.cdi.settings.FileSettingsLoader;

import static org.junit.Assert.*;
import static org.hamcrest.core.IsEqual.*;

public class FileSettingsLoader_Test {
	
	private FileSettingsLoader loader;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void beforeEachTest() {
		loader = new FileSettingsLoader();
	}
	
	private Properties properties(String resource) {
		Properties properties = new Properties();
		properties.setProperty(FileSettingsLoader.RESOURCE_PROPERTY, resource);
		
		return properties;
	}
	
	@Test
	public void loadUnknownFile() throws Exception {
		Properties properties = properties("UnknownFile.txt");
		
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(equalTo("File with settings can not be found."));
		
		loader.currentSettingsDocument(properties);
	}
	
	@Test
	public void loadFile_empty() throws Exception {
		Properties properties = properties("EmptyFile.txt");
		
		String document = loader.currentSettingsDocument(properties);
		
		assertEquals("", document);
	}
	
	@Test
	public void loadFile_UTF8() throws Exception {
		Properties properties = properties("TestFile_UTF8.txt");
		
		String document = loader.currentSettingsDocument(properties);
		
		assertEquals("ěščřžýáíé", document);
	}
	
}