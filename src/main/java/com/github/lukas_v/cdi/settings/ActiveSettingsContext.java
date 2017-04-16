package com.github.lukas_v.cdi.settings;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

/**
 * 
 * @author lukas-v
 */
class ActiveSettingsContext implements Context {
	
	private final Map<Class<?>, Object> snapshotOfSettings;
	
	ActiveSettingsContext(Map<Class<?>, Object> snapshotOfSettings) {
		this.snapshotOfSettings = Objects.requireNonNull(snapshotOfSettings);
	}
	
	@Override
	public Class<? extends Annotation> getScope() {
		return SettingsScope.class;
	}
	
	@Override
	public <T> T get(Contextual<T> contextual) {
		return null;
	}
	
	@Override
	public <T> T get(Contextual<T> contextual, CreationalContext<T> context) {
		Bean<T> bean = (Bean<T>)contextual;
		
		Class<?> beanClass = bean.getBeanClass();
		
		@SuppressWarnings("unchecked")
		T instance = (T) snapshotOfSettings.get(beanClass);
		if(instance != null)
		{
			Optional<T> clone = Utils.cloneObject(instance);
			if(clone.isPresent()) {
				return clone.get();
			}
		}
		
		return instance;
	}
	
	@Override
	public boolean isActive() {
		return true;
	}
	
	@Override
	public String toString() {
		return "ActiveSettingsContext [snapshotOfSettings=" + snapshotOfSettings + "]";
	}
	
}