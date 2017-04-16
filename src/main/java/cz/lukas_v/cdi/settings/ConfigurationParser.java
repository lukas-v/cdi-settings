package cz.lukas_v.cdi.settings;

import java.util.Collection;
import java.util.Map;

/**
 * 
 * @author lukas-v
 */
interface ConfigurationParser {
	
	public Map<Class<?>, Object> parseConfiguration(String configuration, Collection<Class<?>> beanClasses);
	
}