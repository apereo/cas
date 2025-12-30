package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.DynamoDbPropertySourceLocator;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link CasDynamoDbCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "dynamodb")
@AutoConfiguration
public class CasDynamoDbCloudConfigBootstrapAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbPropertySourceLocator")
    public PropertySourceLocator dynamoDbPropertySourceLocator() {
        return new DynamoDbPropertySourceLocator();
    }
}
