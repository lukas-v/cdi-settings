package cz.lukas_v.cdi.settings;

import static cz.lukas_v.cdi.settings.Utils.lookupForBeanManager;
import static cz.lukas_v.cdi.settings.Utils.verifyBeanDefinition;

import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * 
 * @author lukas-v
 */
public final class Settings {
	
	private Settings() {}
	
	/**
	 * 
	 * 
	 * @param settingsClass type of settings to return
	 * 
	 * @throws NullPointerException thrown in case that given class is null
	 * 
	 * @throws IllegalArgumentException thrown in case that given class is not
	 *         proper class annotated with {@link SettingsScope}
	 * 
	 * @throws IllegalStateException thrown in case that {@link BeanManager}
	 *         can not be found or the {@link Bean#getBeanClass()} does not
	 *         relate to the given class
	 * 
	 * @return an instance of given settings class or {@link Optional#empty()}
	 *         in case that bean manager does not know about given type.
	 */
	public static <T> Optional<T> snapshotOf(Class<T> settingsClass) {
		verifyBeanDefinition(settingsClass);
		
		BeanManager beanManager = lookupForBeanManager();
		if(beanManager == null) {
			throw new IllegalStateException("Bean manager can not be found.");
		}
		
		Set<Bean<?>> beans = beanManager.getBeans(settingsClass);
		if(beans.isEmpty()) {
			return Optional.empty();
		}
		
		Bean<?> bean = beans.iterator().next();
		if(!settingsClass.isAssignableFrom(bean.getBeanClass())) {
			throw new IllegalStateException();
		}
		
        CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
        
        @SuppressWarnings("unchecked")
		T instance = (T) beanManager.getReference(bean, settingsClass, ctx);
		
		return Optional.of(instance);
	}
	
}