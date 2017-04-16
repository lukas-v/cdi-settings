package com.github.lukas_v.cdi.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;

/**
 * 
 * @author lukas-v
 */
class ConfigurationParser_Typesafe implements ConfigurationParser {
	
	ConfigurationParser_Typesafe() {}
	
	@Override
	public Map<Class<?>, Object> parseConfiguration(String configuration, Collection<Class<?>> beanClasses) {
		Config config = ConfigFactory.parseString(configuration);
		
		Map<Class<?>, Object> instancesMap = new HashMap<>();
		for(Class<?> beanClass : beanClasses)
		{
			instancesMap.put
			(
				beanClass, 
				instantiateSetting(config, beanClass)
			);
		}
		
		return Collections.unmodifiableMap(instancesMap);
	}
	
	private <T> T instantiateSetting(Config config, Class<T> beanClass) {
		String namespace = Utils.scopeNamespace(beanClass);
		
		Config namespaceConfig = config.getConfig(namespace);
		
		return ConfigBeanFactory.create(namespaceConfig, beanClass);
	}
	
}