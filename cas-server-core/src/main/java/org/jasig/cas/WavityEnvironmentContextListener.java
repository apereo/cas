package org.jasig.cas;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is for creating Wavity environment for using broker API
 * 
 * @author davidlee
 */
@WebListener
public class WavityEnvironmentContextListener implements ServletContextListener {
	
	/**
	 * logger
	 */
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/**
	 * Wavity environment configuration directory
	 */
	public static final String CONFIG_DIR = "wavity.config.dir";
	
	/**
	 * Initial parameter which is defined in the deployment descriptor (web.xml)
	 */
	private static final String INIT_PARAM_WAVITY_CONFIG_DIR = "wavityConfigLocation";
	
	public WavityEnvironmentContextListener() {
		super();
		log.debug("[{}] initialized...", WavityEnvironmentContextListener.class.getSimpleName());
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		final ServletContext ctx = event.getServletContext();
		ctx.log("looking for configuration dir system property -D" + CONFIG_DIR);
		String configDir = System.getProperty(CONFIG_DIR);
		if (StringUtils.isBlank(configDir)) {
			ctx.log("*** configuration dir system property not set, try context init parameter ***");
			configDir = ctx.getInitParameter(INIT_PARAM_WAVITY_CONFIG_DIR);
			if (StringUtils.isBlank(configDir)) {
				throw new IllegalArgumentException(
	    				"Mandatory parameter 'wavityConfigLocation' is missing in ConfigurationFilter declaration in web.xml" );
			}
			if (configDir.startsWith("/WEB-INF")) {
    			configDir = ctx.getRealPath(configDir);
    		}
		}
		File dir = new File(configDir);
		if (!dir.exists()) {
			ctx.log("provided configuration folder does not exist, creating it:" + configDir);
			boolean created = dir.mkdirs();
			if (!created) {
    			throw new IllegalArgumentException("Could not create the given config directory " + configDir);
    		}
		}
		ctx.log("... using configuration dir: " + dir.getAbsolutePath());
		System.setProperty(CONFIG_DIR, dir.getAbsolutePath());
		
		// Initialize Wavity environment for using the plan provider and the broker provider
	    WavityEnvInitializer wavityEnvInitializer = new WavityEnvInitializer();
	    try {
	    	wavityEnvInitializer.setUp();
	    } catch (Exception e) {
	    	log.error("failed to initialize wavity enviroment");
	    }
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}
	
}
