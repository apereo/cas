package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.AmazonSimpleSystemsManagementPropertySourceLocator;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.services.ssm.SsmClient;

/**
 * This is {@link CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@Getter
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "aws-ssm")
@AutoConfiguration
public class CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfiguration {
    /**
     * Configuration prefix for amazon secrets manager.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.aws.ssm";

    @Bean
    @ConditionalOnMissingBean(name = "amazonSimpleSystemsManagementPropertySourceLocator")
    public PropertySourceLocator amazonSimpleSystemsManagementPropertySourceLocator(
        final Environment environment) {
        val builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
        val ssmClient = builder.build(SsmClient.builder(), SsmClient.class);
        return new AmazonSimpleSystemsManagementPropertySourceLocator(ssmClient);
    }
}
