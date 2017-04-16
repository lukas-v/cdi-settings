package com.github.lukas_v.cdi.settings;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 * 
 * @author lukas-v
 */
class InactiveSettingsContext implements Context {
	
	InactiveSettingsContext() {}
	
	@Override
	public Class<? extends Annotation> getScope() {
		return SettingsScope.class;
	}
	
	@Override
	public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		throw new ContextNotActiveException();
	}
	
	@Override
	public <T> T get(Contextual<T> contextual) {
		throw new ContextNotActiveException();
	}
	
	@Override
	public boolean isActive() {
		return false;
	}
	
	@Override
	public String toString() {
		return "InactiveSettingsContext";
	}
	
}