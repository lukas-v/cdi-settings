package com.github.lukas_v.cdi.settings;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Class allows to read configuration by execution of native SQL.
 * 
 * @author lukas-v
 */
public class JpaSettingsLoader implements SettingsSource {
	
	public static final String ENTITY_MANAGER = "settings.loader.jpa.entity-manager-factory";
	public static final String NATIVE_SQL = "settings.loader.jpa.nativeSql";
	
	@Override
	public String currentSettingsDocument(Properties settings) {
		String emfName = settings.getProperty(ENTITY_MANAGER);
		if(emfName == null || emfName.isEmpty()) {
			throw new IllegalArgumentException("Parameter with entity manager is missing.");
		}
		
		String nativeSql = settings.getProperty(NATIVE_SQL);
		if(nativeSql == null || nativeSql.isEmpty()) {
			throw new IllegalArgumentException("Parameter with native SQL is missing.");
		}
		
		EntityManager em = null;
		try
		{
			EntityManagerFactory emf = Utils
				.lookupForEntityManagerFactory
				(
					emfName
				);
			
			em = emf.createEntityManager();
			
			return (String)em
				.createNativeQuery(nativeSql)
				.getSingleResult();
			
		} catch(Exception ex) {
			throw new RuntimeException(ex);
			
		} finally {
			if(em != null) {
				em.close();
			}
		}
	}
	
}