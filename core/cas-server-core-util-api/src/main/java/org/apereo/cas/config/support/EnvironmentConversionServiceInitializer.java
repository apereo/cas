package org.apereo.cas.config.support;

import org.apereo.cas.CasEmbeddedValueResolver;

import lombok.val;
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
    public void initialize(final ConfigurableApplicationContext ctx) {
        val conversionService = new DefaultFormattingConversionService(true);
        conversionService.setEmbeddedValueResolver(new CasEmbeddedValueResolver(ctx));
        ctx.getEnvironment().setConversionService(conversionService);
    }
}
