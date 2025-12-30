package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.AmazonSecretsManagerPropertySourceLocator;
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
import org.springframework.core.env.ConfigurableEnvironment;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * This is {@link CasAmazonSecretsManagerCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "aws-secretsmanager")
@AutoConfiguration
public class CasAmazonSecretsManagerCloudConfigBootstrapAutoConfiguration {
    /**
     * Configuration prefix for amazon secrets manager.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.aws.secrets-manager";

    @Bean
    @ConditionalOnMissingBean(name = "amazonSecretsManagerPropertySourceLocator")
    public PropertySourceLocator amazonSecretsManagerPropertySourceLocator(
        final ConfigurableEnvironment environment) {
        val builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
        val secretsManager = builder.build(SecretsManagerClient.builder(), SecretsManagerClient.class);
        return new AmazonSecretsManagerPropertySourceLocator(secretsManager);
    }
}
