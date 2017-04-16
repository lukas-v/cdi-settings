package cz.lukas_v.cdi.settings;

import cz.lukas_v.cdi.settings.SettingsScope;

@SettingsScope(namespace="dummy")
public class DummySettings_FailingCloneable implements Cloneable {
	
	private static interface CloneFailure {
		
		Object fail() throws CloneNotSupportedException;
		
	}
	
	private static CloneFailure failure = () -> {
		throw new CloneNotSupportedException();
	};
	
	public static void failOnCloneNotSupported() {
		failure = () -> {
			throw new CloneNotSupportedException();
		};
	}
	
	public static void failOnNullPointerException() {
		failure = () -> {
			throw new NullPointerException();
		};
	}
	
	public static void returnNull() {
		failure = () -> {
			return null;
		};
	}
	
	public static void returnDirrefentClass() {
		failure = () -> {
			return "surprise";
		};
	}
	
	private String value;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return failure.fail();
	}
	
}