package org.apereo.cas.config;

import org.apereo.cas.util.ApplicationContextProvider;
import org.apereo.cas.util.SpringAwareMessageMessageInterpolator;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.MessageInterpolator;

/**
 * This is {@link CasCoreUtilConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreUtilConfiguration")
public class CasCoreUtilConfiguration {
    
    @Bean
    public ApplicationContextAware applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    public static MessageInterpolator messageInterpolator() {
        return new SpringAwareMessageMessageInterpolator();
    }
}
