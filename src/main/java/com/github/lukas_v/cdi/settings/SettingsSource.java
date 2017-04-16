package com.github.lukas_v.cdi.settings;

import java.util.Properties;

/**
 * Common interface for all classes that provide the ability
 * to load string representation of settings from implementation
 * dependent source.
 * 
 * @author lukas-v
 */
public interface SettingsSource {
	
	/**
	 * Method returns string representation of current settings.
	 * 
	 * @param properties library configuration that may be used
	 * 
	 * @throws Exception exception may be thrown in case that something goes wrong
	 * 
	 * @return string representation of current settings
	 */
	public String currentSettingsDocument(Properties properties) throws Exception;
	
}