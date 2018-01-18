package org.apereo.cas.configuration.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.DefaultCasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.springframework.beans.factory.annotation.Autowired;
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
@Configuration("casCoreBootstrapStandaloneLocatorConfiguration")
@Slf4j
public class CasCoreBootstrapStandaloneLocatorConfiguration {

    @Autowired
    private Environment environment;

    @ConditionalOnMissingBean(name = "casConfigurationPropertiesSourceLocator")
    @Bean
    public CasConfigurationPropertiesSourceLocator casConfigurationPropertiesSourceLocator() {
        return new DefaultCasConfigurationPropertiesSourceLocator(casConfigurationCipherExecutor(),
            casConfigurationPropertiesEnvironmentManager());
    }

    @ConditionalOnMissingBean(name = "casConfigurationCipherExecutor")
    @Bean
    public CipherExecutor<String, String> casConfigurationCipherExecutor() {
        return new CasConfigurationJasyptCipherExecutor(environment);
    }

    @ConditionalOnMissingBean(name = "casConfigurationPropertiesEnvironmentManager")
    @Bean
    public CasConfigurationPropertiesEnvironmentManager casConfigurationPropertiesEnvironmentManager() {
        return new CasConfigurationPropertiesEnvironmentManager();
    }
}
