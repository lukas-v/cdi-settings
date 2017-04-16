package cz.lukas_v.cdi.settings;

import cz.lukas_v.cdi.settings.SettingsScope;

@SettingsScope(namespace="dummy")
public class DummySettings_Cloneable implements Cloneable {
	
	private String value;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public DummySettings_Cloneable clone() throws CloneNotSupportedException {
		return (DummySettings_Cloneable)super.clone();
	}
	
}