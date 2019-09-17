package org.apereo.cas.configuration.config;

import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * This is {@link CasCoreEnvironmentConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casCoreEnvironmentConfiguration", proxyBeanMethods = false)
public class CasCoreEnvironmentConfiguration {

    @Autowired
    private ConfigurationPropertiesBindingPostProcessor binder;

    @Autowired
    private Environment environment;

    @ConditionalOnMissingBean(name = "configurationPropertiesEnvironmentManager")
    @Bean
    public CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager() {
        return new CasConfigurationPropertiesEnvironmentManager(binder, environment);
    }
}
