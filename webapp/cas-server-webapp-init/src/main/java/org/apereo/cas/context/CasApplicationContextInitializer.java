package org.apereo.cas.context;

import module java.base;
import org.apereo.cas.web.CasWebApplicationReady;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link CasApplicationContextInitializer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
public class CasApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    /**
     * System property to indicate whether configuration status has passed validation.
     */
    public static final String SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS = CasWebApplicationReady.SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS;

    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        val activeProfiles = List.of(applicationContext.getEnvironment().getActiveProfiles());
        LOGGER.debug("Initializing application context [{}] for active profiles [{}]",
            applicationContext.getDisplayName(), activeProfiles);
        System.clearProperty(SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS);
    }
}
