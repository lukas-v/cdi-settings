package cz.lukas_v.cdi.settings;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Class allows to read configuration from a resource in UTF-8.
 * 
 * @author lukas-v
 */
public class FileSettingsLoader implements SettingsSource {
	
	public static final String RESOURCE_PROPERTY = "settings.loader.file.resource";
	
	@Override
	public String currentSettingsDocument(Properties settings) throws Exception {
		String resource = settings.getProperty(RESOURCE_PROPERTY);
		
		URL url = Utils.getResource(resource);
		if(url == null) {
			throw new IllegalArgumentException("File with settings can not be found.");
		}
		
		try
		(
			InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader buffer = new BufferedReader(isr);
		) {
            return buffer
            		.lines()
            		.collect(Collectors.joining("\n"));
        }
	}
	
}