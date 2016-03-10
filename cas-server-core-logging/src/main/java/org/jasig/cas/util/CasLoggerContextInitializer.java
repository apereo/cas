package org.jasig.cas.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initialize the CAS logging framework by updating the location of the log configuration file.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@WebListener
@Component("log4jInitialization")
public final class CasLoggerContextInitializer extends AbstractServletContextInitializer {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private static final Logger LOGGER = LoggerFactory.getLogger(CasLoggerContextInitializer.class);

    @Value("${log4j.config.location:classpath:log4j2.xml}")
    private final Resource logConfigurationFile;

    /**
     * Instantiates a new Cas logger context initializer.
     */
    public CasLoggerContextInitializer() {
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


    @Override
    public void initializeRootApplicationContext() {
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
}
