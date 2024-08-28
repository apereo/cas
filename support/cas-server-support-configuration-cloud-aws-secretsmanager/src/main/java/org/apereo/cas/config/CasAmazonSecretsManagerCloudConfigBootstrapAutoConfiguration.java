package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

import java.util.Properties;

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
public class CasAmazonSecretsManagerCloudConfigBootstrapAutoConfiguration implements PropertySourceLocator {
    /**
     * Configuration prefix for amazon secrets manager.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.aws.secrets-manager";

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val props = new Properties();
        try {
            val builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
            val secretsManager = builder.build(SecretsManagerClient.builder(), SecretsManagerClient.class);
            val listRequest = ListSecretsRequest.builder().build();
            val listResults = secretsManager.listSecrets(listRequest);
            val secretList = listResults.secretList();
            if (secretList != null && !secretList.isEmpty()) {
                LOGGER.debug("Fetched [{}] secret(s)", secretList.size());
                secretList
                    .stream()
                    .map(SecretListEntry::name)
                    .forEach(name -> {
                        LOGGER.debug("Fetching secret [{}]", name);
                        val getRequest = GetSecretValueRequest.builder().secretId(name).build();
                        val result = secretsManager.getSecretValue(getRequest);
                        if (result != null) {
                            props.put(name, result.secretString());
                        }
                    });
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        LOGGER.debug("Located [{}] secret(s)", props.size());
        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }
}
