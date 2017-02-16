package org.apereo.cas.config;

import org.apereo.cas.config.support.CasConfigurationEmbeddedValueResolver;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringAwareMessageMessageInterpolator;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import javax.annotation.PostConstruct;
import javax.validation.MessageInterpolator;

/**
 * This is {@link CasCoreUtilConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreUtilConfiguration")
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableScheduling
public class CasCoreUtilConfiguration {

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    public MessageInterpolator messageInterpolator() {
        return new SpringAwareMessageMessageInterpolator();
    }

    @Bean
    public CommunicationsManager communicationsManager() {
        return new CommunicationsManager();
    }

    @PostConstruct
    public void init() {
        final ConfigurableApplicationContext applicationContext = applicationContextProvider().getConfigurableApplicationContext();
        final DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(true);
        applicationContext.getEnvironment().setConversionService(conversionService);
        final ScheduledAnnotationBeanPostProcessor p = applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class);
        p.setEmbeddedValueResolver(new CasConfigurationEmbeddedValueResolver(applicationContext));
    }


}
