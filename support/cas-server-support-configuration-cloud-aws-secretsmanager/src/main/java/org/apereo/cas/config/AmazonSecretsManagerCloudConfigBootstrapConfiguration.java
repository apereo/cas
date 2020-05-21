package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
@Configuration(value = "amazonSecretsManagerCloudConfigBootstrapConfiguration", proxyBeanMethods = false)
@Slf4j
@Getter
public class AmazonSecretsManagerCloudConfigBootstrapConfiguration implements PropertySourceLocator {
    /**
     * Configuration prefix for amazon secrets manager.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.aws.secretsManager";

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val props = new Properties();
        try {
            val builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
            val secretsManager = builder.build(AWSSecretsManagerClientBuilder.standard(), AWSSecretsManager.class);
            val listRequest = new ListSecretsRequest();
            val listResults = secretsManager.listSecrets(listRequest);
            val secretList = listResults.getSecretList();
            if (secretList != null && !secretList.isEmpty()) {
                LOGGER.debug("Fetched [{}] secret(s)", secretList.size());
                secretList
                    .stream()
                    .map(SecretListEntry::getName)
                    .forEach(name -> {
                        LOGGER.debug("Fetching secret [{}]", name);
                        val getRequest = new GetSecretValueRequest().withSecretId(name);
                        val result = secretsManager.getSecretValue(getRequest);
                        if (result != null) {
                            props.put(name, result.getSecretString());
                        }
                    });
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        LOGGER.debug("Located [{}] secret(s)", props.size());
        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }
}
