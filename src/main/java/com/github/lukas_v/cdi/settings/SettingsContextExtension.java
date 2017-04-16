package cz.lukas_v.cdi.settings;

import static cz.lukas_v.cdi.settings.Utils.*;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author lukas-v
 */
public final class SettingsContextExtension implements Extension {
	
	private final Logger logger = LoggerFactory.getLogger(SettingsContextExtension.class);
	
    private ExecutorService executorService;
    private Set<Class<?>> discoveredSettings;
    
    private final Object lock = new Object();
	private SettingsContext context;
	
	public SettingsContextExtension() {}
	
	protected void beforeBeanDiscovery(@Observes BeforeBeanDiscovery abd) {
		executorService = Utils.lookupForExecutor();
		if(executorService == null) {
			logger.warn("Default executor service was not found, reloads are going to be executed directly.");
		}
		
		discoveredSettings = new HashSet<>();
	}
	
	/**
	 * Method observes all types annotated with the {@link SettingsScope}.
	 * 
	 * @param pat
	 */
	protected <T> void collectUserConfigSources(@Observes @WithAnnotations(SettingsScope.class) ProcessAnnotatedType<T> pat) {
		Class<?> annotatedBean = pat.getAnnotatedType().getJavaClass();
		
		logger.debug("New bean with settings has been discovered: {{}}", annotatedBean.getClass().getName());
		
		verifyBeanDefinition(annotatedBean);
		String namespace = verifyScopeNamespace(annotatedBean);
		
		discoveredSettings.add(annotatedBean);
	}
	
	/**
	 * Method that is supposed to be called after all the beans are discovered
	 * which registers new context instance for {@link SettingsScope}.
	 * 
	 * @param event
	 * @param manager
	 */
	protected void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
		if(!discoveredSettings.isEmpty())
		{
			StringJoiner joiner = new StringJoiner("\n\t", "\n[\n\t", "\n]");
			for(Class<?> discoveredBean : discoveredSettings) {
				joiner.add(discoveredBean.getName());
			}
			
			logger.info("Following beans with settings have been discovered: {}", joiner);
		}
		else {
			logger.warn("No bean with settings has been discovered.");
		}
		
		synchronized(lock) {
			context = new SettingsContext(discoveredSettings);
		}
		
		event.addContext(context);
	}
	
	/**
	 * Method that is supposed to be called after the deployment has been
	 * successfully validated. This is the right time to initialize all
	 * beans with initial snapshot of settings that they hold.
	 * 
	 * @param event
	 * @param manager
	 */
	protected void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager manager) {
		synchronized(lock)
		{
			try {
				context.initialize();
				context.reloadSettings();
			} catch(Exception ex) {
				throw new IllegalStateException("Unable to load initial settings.", ex);
			}
		}
	}
	
	/**
	 * Method that is used to observe the time when the current snapshot of
	 * settings is considered as out of sync and there is a need to create
	 * new snapshot of all beans.
	 *  
	 * @param event
	 */
	protected void settingsHaveBeenInvalidated(@Observes(during=TransactionPhase.AFTER_SUCCESS) ReloadSettings event) {
		logger.info("Reload of registered beans with settings has been triggered.");
		
		if(executorService != null) {
			executorService.submit(this::reloadSettings);
		}
		else {
			reloadSettings();
		}
	}
	
	protected void beforeShutdown(@Observes BeforeShutdown event, BeanManager manager) {
		synchronized(lock)
		{
			if(context != null)
			{
				context.dispose();
				context = null;
			}
		}
	}
	
	private void reloadSettings() {
		synchronized(lock)
		{
			if(context != null)
			{
				logger.info("Registered beans with settings are going to be actualized.");
				
				try {
					context.reloadSettings();
				} catch(Exception ex) {
					logger.error("Exception while reloading registered beans.", ex);
				}
			}
			else {
				logger.warn("There is no context present, actualization is not possible.");
			}
		}
	}
	
}