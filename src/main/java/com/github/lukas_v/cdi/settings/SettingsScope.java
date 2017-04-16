package cz.lukas_v.cdi.settings;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.inject.Stereotype;
import javax.inject.Scope;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * 
 * @author lukas-v
 */
@Target(TYPE)
@Retention(RUNTIME)
@Scope
/*
 * {@link Stereotype} must be used because {@link Scope} would not be found in some cases:
 *   https://developer.jboss.org/thread/262789?_sscc=t
 *   https://issues.jboss.org/browse/CDI-420
 *   http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#type_discovery_steps
 *   https://issues.jboss.org/browse/CDI-420?focusedCommentId=13046872&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-13046872
 */
@Stereotype
public @interface SettingsScope {
	
	/**
	 * Allows to override default behavior of using
	 * {@link Class#getSimpleName()} as a namespace
	 * for properties in annotated class.
	 */
	String namespace() default "";
	
}