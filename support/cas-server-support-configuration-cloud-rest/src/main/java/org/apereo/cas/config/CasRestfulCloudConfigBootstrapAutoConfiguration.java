package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link CasRestfulCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "rest")
@AutoConfiguration
public class CasRestfulCloudConfigBootstrapAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "restfulPropertySourceLocator")
    public PropertySourceLocator restfulPropertySourceLocator() {
        return new RestfulPropertySourceLocator();
    }
}
