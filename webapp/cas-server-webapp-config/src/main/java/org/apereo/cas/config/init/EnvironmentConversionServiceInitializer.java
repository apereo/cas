package org.apereo.cas.config.init;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.format.support.DefaultFormattingConversionService;

/**
 * This is {@link EnvironmentConversionServiceInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class EnvironmentConversionServiceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        final DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(true);
        applicationContext.getEnvironment().setConversionService(conversionService);
    }
}
