package org.apereo.cas.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Properties;

/**
 * This is {@link AmazonSecretsManagerCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("amazonSecretsManagerCloudConfigBootstrapConfiguration")
@Slf4j
@Getter
public class AmazonSecretsManagerCloudConfigBootstrapConfiguration implements PropertySourceLocator {
    private static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.aws.secretsManager";

    @Override
    public PropertySource<?> locate(final Environment environment) {
        final var props = new Properties();
        try {
            final var builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
            final var secretsManager = builder.build(AWSSecretsManagerClientBuilder.standard(), AWSSecretsManager.class);
            final var listRequest = new ListSecretsRequest();
            final var listResults = secretsManager.listSecrets(listRequest);
            final var secretList = listResults.getSecretList();
            if (secretList != null && secretList.isEmpty()) {
                LOGGER.debug("Fetched [{}] secret(s)", secretList.size());
                secretList
                    .stream()
                    .map(SecretListEntry::getName)
                    .forEach(name -> {
                        LOGGER.debug("Fetching secret [{}]", name);
                        final var getRequest = new GetSecretValueRequest().withSecretId(name);
                        final var result = secretsManager.getSecretValue(getRequest);
                        if (result != null) {
                            props.put(name, result.getSecretString());
                        }
                    });
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("Located [{}] secret(s)", props.size());
        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }
}
