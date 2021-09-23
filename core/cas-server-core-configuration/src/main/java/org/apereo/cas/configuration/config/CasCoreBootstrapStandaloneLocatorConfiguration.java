package org.apereo.cas.configuration.config;

import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.DefaultCasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.loader.ConfigurationPropertiesLoaderFactory;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * This is {@link CasCoreBootstrapStandaloneLocatorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Profile("standalone")
@ConditionalOnProperty(value = "spring.cloud.config.enabled", havingValue = "false")
@Configuration(value = "casCoreBootstrapStandaloneLocatorConfiguration", proxyBeanMethods = false)
public class CasCoreBootstrapStandaloneLocatorConfiguration {

    @ConditionalOnMissingBean(name = "casConfigurationPropertiesSourceLocator")
    @Bean
    @Autowired
    public CasConfigurationPropertiesSourceLocator casConfigurationPropertiesSourceLocator(
        @Qualifier("configurationPropertiesLoaderFactory")
        final ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory,
        @Qualifier("configurationPropertiesEnvironmentManager")
        final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager) {
        return new DefaultCasConfigurationPropertiesSourceLocator(
            configurationPropertiesEnvironmentManager,
            configurationPropertiesLoaderFactory);
    }

    @ConditionalOnMissingBean(name = "casConfigurationCipherExecutor")
    @Bean
    @Autowired
    public CipherExecutor<String, String> casConfigurationCipherExecutor(
        final Environment environment) {
        return new CasConfigurationJasyptCipherExecutor(environment);
    }

    @ConditionalOnMissingBean(name = "configurationPropertiesLoaderFactory")
    @Bean
    @Autowired
    public ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory(
        @Qualifier("casConfigurationCipherExecutor")
        final CipherExecutor<String, String> casConfigurationCipherExecutor,
        final Environment environment) {
        return new ConfigurationPropertiesLoaderFactory(casConfigurationCipherExecutor, environment);
    }

}
