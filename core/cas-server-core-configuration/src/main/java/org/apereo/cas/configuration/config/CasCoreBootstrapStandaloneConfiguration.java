package org.apereo.cas.configuration.config;

import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.CommaSeparatedStringToThrowablesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ResourceLoader;

import java.util.List;

/**
 * This is {@link CasCoreBootstrapStandaloneConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Profile("standalone")
@ConditionalOnProperty(value = "spring.cloud.config.enabled", havingValue = "false")
@Configuration("casStandaloneBootstrapConfiguration")
public class CasCoreBootstrapStandaloneConfiguration implements PropertySourceLocator, PriorityOrdered {

    @Autowired
    private ResourceLoader resourceLoader;

    @ConfigurationPropertiesBinding
    @Bean
    public Converter<String, List<Class<? extends Throwable>>> commaSeparatedStringToThrowablesCollection() {
        return new CommaSeparatedStringToThrowablesConverter();
    }

    @Bean
    public CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager() {
        return new CasConfigurationPropertiesEnvironmentManager();
    }

    @Override
    public PropertySource<?> locate(final Environment environment) {
        final CasConfigurationPropertiesSourceLocator locator = new CasConfigurationPropertiesSourceLocator();
        return locator.locate(environment, this.resourceLoader, configurationPropertiesEnvironmentManager());
    }
    
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
