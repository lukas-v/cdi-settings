package com.github.lukas_v.cdi.settings;

import java.nio.file.AccessMode;
import java.util.Arrays;
import java.util.Objects;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

import com.github.lukas_v.cdi.settings.SettingsScope;
import com.github.lukas_v.cdi.settings.Utils;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;

@RunWith(Suite.class)
@SuiteClasses
({
	ClassVerification_Test.ValidValues.class, 
	ClassVerification_Test.InvalidValues.class, 
	ClassVerification_Test.OtherInvalidValues.class
})
public class ClassVerification_Test {
	
	private ClassVerification_Test() {}
	
	public class InnerClass {}
	
	@SettingsScope
	public class AnnotatedInnerClass {}
	
	public static class StaticNestedClass {}
	
	@SettingsScope
	public static class AnnotatedStaticNestedClass {}
	
	protected static abstract class TestBase {
		
		@Rule public ExpectedException exception = ExpectedException.none();
		
	}
	
	@RunWith(Parameterized.class)
	public static class ValidValues extends TestBase {
		
		@Parameters
		public static Iterable<Object[]> data() {
			return Arrays.asList(new Object[][] {
				{ DummySettings_Cloneable.class },
				{ DummySettings_NotCloneable.class }, 
				{ AnnotatedStaticNestedClass.class }
			});
		}
		
		private final Class<?> parameter;
		
		public ValidValues(Class<?> parameter) {
			this.parameter = Objects.requireNonNull(parameter);
		}
		
		@Test
		public void validDefinition() {
			Utils.verifyBeanDefinition(parameter);
		}
		
	}
	
	@RunWith(Parameterized.class)
	public static class InvalidValues extends TestBase {
		
		private static Runnable anonymousClass = new Runnable() {
			
			@Override
			public void run() {}
			
		};
		
		@Parameters
		public static Iterable<Object[]> data() {
			return Arrays.asList(new Object[][] {
				{ Integer.TYPE }, // primitive class type
				{ Runnable.class }, // interface
				{ Override.class }, // annotation
				{ AccessMode.class }, // enumeration
				{ InnerClass.class }, // non-static nested class
				{ AnnotatedInnerClass.class }, // non-static nested class
				{
					new Runnable() {
						
						@Override
						public void run() {}
						
					}.getClass() // local class
				}, 
				{ anonymousClass.getClass() }, // anonymous class
				{ String[].class } // array
			});
		}
		
		private final Class<?> parameter;
		
		public InvalidValues(Class<?> parameter) {
			this.parameter = Objects.requireNonNull(parameter);
		}
		
		@Test
		public void nullValue_illegalArgumentExceptionIsThrown() {
			exception.expect(IllegalArgumentException.class);
			exception.expectMessage(equalTo("Invalid class type given."));
			Utils.verifyBeanDefinition(parameter);
		}
		
	}
	
	public static class OtherInvalidValues extends TestBase {
		
		@Test
		public void nullValue_nullPointerExceptionIsThrown() {
			exception.expect(NullPointerException.class);
			exception.expectMessage(equalTo("Class is required parameter."));
			Utils.verifyBeanDefinition(null);
		}
		
		@Test
		public void classWithoutAnnotation_illegalArgumentExceptionIsThrown() {
			exception.expect(IllegalArgumentException.class);
			exception.expectMessage(startsWith("Class is not annotated with "));
			Utils.verifyBeanDefinition(String.class);
		}
		
		@Test
		public void staticClassWithoutAnnotation_illegalArgumentExceptionIsThrown() {
			exception.expect(IllegalArgumentException.class);
			exception.expectMessage(startsWith("Class is not annotated with "));
			Utils.verifyBeanDefinition(StaticNestedClass.class);
		}
		
	}
	
}