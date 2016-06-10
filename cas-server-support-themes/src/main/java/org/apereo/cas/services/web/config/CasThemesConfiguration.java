package org.apereo.cas.services.web.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.web.RegisteredServiceThemeBasedViewResolver;
import org.apereo.cas.services.web.ServiceThemeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.ViewResolver;

/**
 * This is {@link CasThemesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casThemesConfiguration")
public class CasThemesConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ViewResolver registeredServiceViewResolver() {
        return new RegisteredServiceThemeBasedViewResolver();
    }

    @Bean
    public ThemeResolver serviceThemeResolver() {
        final ServiceThemeResolver resolver = new ServiceThemeResolver();
        resolver.setDefaultThemeName(casProperties.getThemeProperties().getDefaultThemeName());
        return resolver;
    }
}
