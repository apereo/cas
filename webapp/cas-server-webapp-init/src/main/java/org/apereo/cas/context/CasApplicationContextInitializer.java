package org.apereo.cas.context;

import org.apereo.cas.configuration.CasConfigurationPropertiesValidator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;

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
    public static final String SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS = "CONFIG_VALIDATION_STATUS";

    @Override
    public void initialize(@Nonnull final ConfigurableApplicationContext applicationContext) {
        LOGGER.info("Initializing application context [{}]", applicationContext.getDisplayName());
        val validator = new CasConfigurationPropertiesValidator(applicationContext);
        val results = validator.validate();
        System.setProperty(SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS, Boolean.toString(results.isEmpty()));
    }
}
