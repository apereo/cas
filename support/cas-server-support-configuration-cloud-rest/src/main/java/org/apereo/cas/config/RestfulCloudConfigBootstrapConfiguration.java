package org.apereo.cas.config;

import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link RestfulCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "rest")
@AutoConfiguration
public class RestfulCloudConfigBootstrapConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "restfulPropertySourceLocator")
    public PropertySourceLocator restfulPropertySourceLocator() {
        return new RestfulPropertySourceLocator();
    }
}
