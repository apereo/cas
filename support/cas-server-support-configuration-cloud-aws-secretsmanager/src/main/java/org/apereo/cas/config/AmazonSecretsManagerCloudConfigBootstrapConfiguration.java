package org.apereo.cas.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.List;
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

    @Override
    public PropertySource<?> locate(final Environment environment) {
        final Properties props = new Properties();
        final AWSSecretsManager secretsManager = getAmazonSecretsManagerClient(environment);

        try {
            final ListSecretsRequest listRequest = new ListSecretsRequest();
            final ListSecretsResult listResults = secretsManager.listSecrets(listRequest);
            final List<SecretListEntry> secretList = listResults.getSecretList();
            if (secretList != null && secretList.isEmpty()) {
                LOGGER.debug("Fetched [{}] secret(s)", secretList.size());
                secretList
                    .stream()
                    .map(SecretListEntry::getName)
                    .forEach(name -> {
                        LOGGER.debug("Fetching secret [{}]", name);
                        final GetSecretValueRequest getRequest = new GetSecretValueRequest().withSecretId(name);
                        final GetSecretValueResult result = secretsManager.getSecretValue(getRequest);
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

    private static String getSetting(final Environment environment, final String key) {
        return environment.getProperty("cas.spring.cloud.aws.secretsManager." + key);
    }

    private static AWSSecretsManager getAmazonSecretsManagerClient(final Environment environment) {
        final String key = getSetting(environment, "credentialAccessKey");
        final String secret = getSetting(environment, "credentialSecretKey");
        final AWSCredentialsProvider credentials = ChainingAWSCredentialsProvider.getInstance(key, secret);
        String region = getSetting(environment, "region");
        final Region currentRegion = Regions.getCurrentRegion();
        if (currentRegion != null && StringUtils.isBlank(region)) {
            region = currentRegion.getName();
        }
        String regionOverride = getSetting(environment, "regionOverride");
        if (currentRegion != null && StringUtils.isNotBlank(regionOverride)) {
            regionOverride = currentRegion.getName();
        }
        final String endpoint = getSetting(environment, "endpoint");
        final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.standard()
            .withCredentials(credentials)
            .withRegion(region);

        
        if (StringUtils.isNotBlank(endpoint) && StringUtils.isNotBlank(regionOverride)) {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, regionOverride));
        }
        return builder.build();
    }
}
