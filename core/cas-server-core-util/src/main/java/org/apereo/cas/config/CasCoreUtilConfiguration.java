package org.apereo.cas.config;

import org.apereo.cas.util.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * This is {@link CasCoreUtilConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreUtilConfiguration")
public class CasCoreUtilConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreUtilConfiguration.class);

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @PostConstruct
    public void init() {
        final ConfigurableApplicationContext applicationContext = applicationContextProvider().getConfigurableApplicationContext();
        final ScheduledAnnotationBeanPostProcessor p = applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class);
        p.setEmbeddedValueResolver(new EmbeddedValueResolver(applicationContext.getBeanFactory()) {
            @Override
            public String resolveStringValue(final String strVal) {
                final String value = super.resolveStringValue(strVal);
                try {
                    final ConversionService service = applicationContext.getEnvironment().getConversionService();
                    final Duration dur = service.convert(value, Duration.class);
                    if (dur != null) {
                        return String.valueOf(dur.toMillis());
                    }
                    return value;
                } catch (final ConversionFailedException e) {
                    LOGGER.trace(e.getMessage());
                    return value;
                }
            }
        });
    }

}
