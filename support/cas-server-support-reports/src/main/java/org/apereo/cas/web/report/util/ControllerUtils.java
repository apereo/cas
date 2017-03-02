package org.apereo.cas.web.report.util;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.Map;

/**
 * This is {@link ControllerUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public final class ControllerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerUtils.class);

    private ControllerUtils() {
    }

    /**
     * Configure model map for config server cloud bus endpoints.
     *
     * @param busProperties          the bus properties
     * @param configServerProperties the config server properties
     * @param path                   the path
     * @param model                  the model
     */
    public static void configureModelMapForConfigServerCloudBusEndpoints(final BusProperties busProperties,
                                                                         final ConfigServerProperties configServerProperties,
                                                                         final String path,
                                                                         final Map model) {
        if (busProperties != null && busProperties.isEnabled()) {
            model.put("refreshEndpoint", path + configServerProperties.getPrefix() + "/cas/bus/refresh");
            model.put("refreshMethod", "GET");
        } else {
            model.put("refreshEndpoint", path + "/status/refresh");
            model.put("refreshMethod", "POST");
        }
    }

    /**
     * Build logger context logger context.
     *
     * @param environment    the environment
     * @param resourceLoader the resource loader
     * @return the logger context
     */
    public static Pair<Resource, LoggerContext> buildLoggerContext(final Environment environment, final ResourceLoader resourceLoader) {
        try {
            final String logFile = environment.getProperty("logging.config");
            LOGGER.debug("Located logging configuration reference in the environment as [{}]", logFile);

            if (StringUtils.isNotBlank(logFile)) {
                final Resource logConfigurationFile = resourceLoader.getResource(logFile);
                LOGGER.debug("Loaded logging configuration resource [{}]. Initializing logger context...", logConfigurationFile);
                final LoggerContext loggerContext = Configurator.initialize("CAS", null, logConfigurationFile.getURI());
                LOGGER.debug("Installing log configuration listener to detect changes and update");
                loggerContext.getConfiguration().addListener(reconfigurable -> loggerContext.updateLoggers(reconfigurable.reconfigure()));
                return Pair.of(logConfigurationFile, loggerContext);
            } else {
                LOGGER.warn("Logging configuration cannot be found in the environment settings");
            }
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
        return null;
    }
}
