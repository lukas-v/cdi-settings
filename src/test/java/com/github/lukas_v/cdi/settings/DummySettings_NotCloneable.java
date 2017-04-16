package com.github.lukas_v.cdi.settings;

import com.github.lukas_v.cdi.settings.SettingsScope;

@SettingsScope(namespace="dummy")
public class DummySettings_NotCloneable {
	
	private String value;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}