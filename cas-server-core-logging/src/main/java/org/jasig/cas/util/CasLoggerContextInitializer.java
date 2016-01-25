package org.jasig.cas.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initializes the CAS logging framework by calling
 * the logger initializer and sets the location of the
 * log configuration file.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component("log4jInitialization")
public final class CasLoggerContextInitializer implements ServletContextAware {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private static final Logger LOGGER = LoggerFactory.getLogger(CasLoggerContextInitializer.class);

    @Value("${log4j.config.location:classpath:log4j2.xml}")
    private final Resource logConfigurationFile;

    /**
     * Instantiates a new Cas logger context initializer.
     */
    protected CasLoggerContextInitializer() {
        this.logConfigurationFile = null;
    }

    /**
     * Instantiates a new Cas logger context initializer.
     *
     * @param logConfigurationFile the log configuration file
     */
    public CasLoggerContextInitializer(@NotNull final Resource logConfigurationFile) {
        this.logConfigurationFile = logConfigurationFile;
    }

    /**
     * Initialize the logger by decorating the context
     * with settings for the log file and context.
     * Calls the initializer of the logging framework
     * to start the logger.
     */
    private void initialize() {
        if (this.logConfigurationFile == null || !this.logConfigurationFile.exists()) {
            throw new RuntimeException("Log4j configuration file cannot be located");
        }

        try {
            if (!INITIALIZED.get()) {
                final LoggerContext context = (LoggerContext) LogManager.getContext(false);
                final URI oldLocation = context.getConfigLocation();
                final URI location = logConfigurationFile.getURI();
                if (!location.equals(oldLocation)) {
                    context.setConfigLocation(location);
                    LOGGER.debug("Updated logging config file from [{}] to [{}]", oldLocation, location);
                }
                INITIALIZED.set(true);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>Prepared the logger context with the
     * received servlet web context. Because the context
     * may be initialized twice, there are safety checks
     * added to ensure we don't create duplicate log
     * environments.</p>
     * @param servletContext the servlet context
     */
    @Override
    public void setServletContext(final ServletContext servletContext) {
        initialize();
    }
}
