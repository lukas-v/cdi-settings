package cz.lukas_v.cdi.settings;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Utils {
	
	private static final Logger logger = LoggerFactory.getLogger(Utils.class);
	
	private Utils() {}
	
	static ManagedExecutorService lookupForExecutor() {
		// default lookup
		ManagedExecutorService executor = lookupFor("java:comp/DefaultManagedExecutorService");
		
		if(executor == null)
		{
			/* 
			 * default lookup for Wildfly
			 *  - http://planet.jboss.org/post/wildfly_8_and_jsr_236
			 */
			executor = lookupFor("java:jboss/ee/concurrency/scheduler/default");
		}
		
		return executor;
	}
	
	static BeanManager lookupForBeanManager() {
		return lookupFor("java:comp/BeanManager");
	}
	
	static EntityManagerFactory lookupForEntityManagerFactory(String name) {
		return (EntityManagerFactory)lookupFor(name);
	}
	
	private static <T> T lookupFor(String name) {
		try {
			@SuppressWarnings("unchecked")
			T instance = (T) new InitialContext().lookup(name);
			
			return instance;
		} catch(NamingException ne) {}
		
		return null;
	}
	
	
	static void verifyBeanDefinition(Class<?> annotatedBean) {
		if(annotatedBean == null) {
			throw new NullPointerException("Class is required parameter.");
		}
		
		if(annotatedBean.isInterface()
			|| annotatedBean.isAnnotation()
			|| annotatedBean.isEnum()
			|| annotatedBean.isPrimitive()
			|| annotatedBean.isSynthetic()
			|| annotatedBean.isArray()
			|| annotatedBean.isLocalClass()
			|| annotatedBean.isAnonymousClass()
			|| (
				annotatedBean.getEnclosingClass() != null
					&& !Modifier.isStatic(annotatedBean.getModifiers())
			)) {
			throw new IllegalArgumentException("Invalid class type given.");
		}
		
		SettingsScope scope = annotatedBean.getDeclaredAnnotation(SettingsScope.class);
		if(scope == null)
		{
			throw new IllegalArgumentException
			(
				"Class is not annotated with " + SettingsScope.class.getSimpleName()
			);
		}
	}
	
	static String scopeNamespace(Class<?> annotatedBean) {
		SettingsScope scope = annotatedBean.getDeclaredAnnotation(SettingsScope.class);
		
		return !scope.namespace().isEmpty()
			? scope.namespace()
			: annotatedBean.getSimpleName();
	}
	
	static String verifyScopeNamespace(Class<?> annotatedBean) {
		String namespace = scopeNamespace(annotatedBean);
		// TODO implementation needed
		return namespace;
	}
	
	
	static Properties loadSettings(String propertiesPath) throws IOException {
		URL resource = getResource(propertiesPath);
		if(resource == null) {
			throw new IllegalArgumentException("Unable to locate properties file.");
		}
		
		try(InputStream stream = resource.openStream())
		{
			Properties properties = new Properties();
			properties.load(stream);
			
			return properties;
		}
	}
	
	static URL getResource(String resourceName) {
		URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
		if(url == null) {
			url = Utils.class.getClassLoader().getResource(resourceName);
		}
		
		if((url == null) && (resourceName != null) && ((resourceName.length() == 0) || (resourceName.charAt(0) != '/'))) { 
			return getResource('/' + resourceName);
		}
		
		return url;
    }
	
	static SettingsSource lookupForLoader(Properties settings) {
		String loaderClassName = settings.getProperty("settings.loader");
		if(loaderClassName == null || loaderClassName.isEmpty()) {
			throw new IllegalArgumentException("There is no implementation of loader defined.");
		}
		
		Class<?> loaderClass;
		try {
			loaderClass = Class.forName(loaderClassName);
		} catch(ClassNotFoundException cnfe) {
			throw new IllegalArgumentException("Implementation of loader has not been found.");
		}
		
		if(!SettingsSource.class.isAssignableFrom(loaderClass)) {
			throw new IllegalArgumentException("Loader implementation does not implement " + SettingsSource.class.getSimpleName());
		}
		
		Constructor<?> constructor;
		try {
			constructor = loaderClass.getConstructor();
		} catch(NoSuchMethodException nsme) {
			throw new IllegalArgumentException("Loader implementation does not have defaut constructor.");
		}
		
		try {
			return (SettingsSource)constructor.newInstance();
		} catch(Exception ex) {
			throw new IllegalStateException("Loader implementation failed to be instantiated.", ex);
		}
	}
	
	/**
	 * Method tries to clone given object in case that it
	 * implements {@link Cloneable} interface correctly, 
	 * {@link Optional#empty()} otherwise.
	 * 
	 * @param instance object to clone
	 * 
	 * @return clone of given object or {@link Optional#empty()}
	 *         if something went wrong
	 */
	static <T> Optional<T> cloneObject(T instance) {
		if(instance instanceof Cloneable)
		{
			Method cloneMethod;
			try {
				cloneMethod = instance.getClass().getMethod("clone");
			} catch(NoSuchMethodException | SecurityException ex) {
				return Optional.empty();
			}
			
			if(Modifier.isPublic(cloneMethod.getModifiers()))
			{
				Object clone;
				try {
					clone = cloneMethod.invoke(instance);
				} catch(InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
					if(ex instanceof InvocationTargetException) {
						logger.error
						(
							"Unable to clone instance of {{}} properly.", 
							instance.getClass().getName(), ex.getCause()
						);
					}
					
					return Optional.empty();
				}
				
				/*
				 * Clone method may return null or object of
				 * different type (not being in instance's class
				 * hierarchy).
				 */
				if(clone != null && instance.getClass().isAssignableFrom(clone.getClass()))
				{
					@SuppressWarnings("unchecked")
					T properClone = (T)clone;
					
					return Optional.of(properClone);
				}
			}
			
		}
		
		return Optional.empty();
	}
	
}