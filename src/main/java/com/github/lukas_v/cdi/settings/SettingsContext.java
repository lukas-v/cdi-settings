package com.github.lukas_v.cdi.settings;

import static com.github.lukas_v.cdi.settings.Utils.*;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 * 
 * @author lukas-v
 */
class SettingsContext implements Context {
	
	private final String settingsPath;
	private final Set<Class<?>> beans;
	
	private Properties settings;
	Properties getSettings() {
		return settings;
	}
	
	private ConfigurationParser parser;
	ConfigurationParser getParser() {
		return parser;
	}
	
	private SettingsSource loader;
	SettingsSource getLoader() {
		return loader;
	}
	
	private volatile Context currentContext = new InactiveSettingsContext();
	
	SettingsContext(Set<Class<?>> beans) {
		this("settings.properties", beans);
	}
	
	SettingsContext(String settingsPath, Set<Class<?>> beans) {
		this.settingsPath = Objects.requireNonNull(settingsPath);
		this.beans = new HashSet<>(beans);
	}
	
	void initialize() throws Exception {
		settings = loadSettings(settingsPath);
		loader = createLoader();
		parser = createParser();
	}
	
	protected ConfigurationParser createParser() {
		return new ConfigurationParser_Typesafe();
	}
	
	protected SettingsSource createLoader() {
		return lookupForLoader(settings);
	}
	
	void reloadSettings() throws Exception {
		if(!beans.isEmpty())
		{
			String document = loader.currentSettingsDocument
			(
				new Properties(settings) // defensive copy
			);
			
			Map<Class<?>, Object> configuration = parser
				.parseConfiguration
				(
					document, 
					new HashSet<>(beans) // defensive copy
				);
			
			currentContext = new ActiveSettingsContext
			(
				new HashMap<>(configuration) // defensive copy
			);
		}
	}
	
	void dispose() {
		currentContext = new InactiveSettingsContext();
	}
	
	@Override
	public Class<? extends Annotation> getScope() {
		return SettingsScope.class;
	}
	
	@Override
	public boolean isActive() {
		return currentContext.isActive();
	}
	
	@Override
	public <T> T get(Contextual<T> contextual) {
		return currentContext.get(contextual);
	}
	
	@Override
	public <T> T get(Contextual<T> contextual, CreationalContext<T> context) {
		return currentContext.get(contextual, context);
	}
	
}