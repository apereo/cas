package org.apereo.cas.aws;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

/**
 * This is {@link AmazonEnvironmentAwareClientBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class AmazonEnvironmentAwareClientBuilder {
    private final String propertyPrefix;

    private final Environment environment;

    /**
     * Gets setting.
     *
     * @param key the key
     * @return the setting
     */
    public String getSetting(final String key) {
        return environment.getProperty(this.propertyPrefix + '.' + key);
    }

    /**
     * Gets setting.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the setting
     */
    public String getSetting(final String key, final String defaultValue) {
        val result = environment.getProperty(this.propertyPrefix + '.' + key);
        return StringUtils.defaultIfBlank(result, defaultValue);
    }

    /**
     * Gets setting.
     *
     * @param <T>        the type parameter
     * @param key        the key
     * @param targetType the target type
     * @return the setting
     */
    public <T> T getSetting(final String key, final Class<T> targetType) {
        return environment.getProperty(this.propertyPrefix + '.' + key, targetType);
    }

    /**
     * Build the client.
     *
     * @param <T>        the type parameter
     * @param builder    the builder
     * @param clientType the client type
     * @return the client instance
     */
    @SneakyThrows
    public <T> T build(final AwsClientBuilder builder, final Class<T> clientType) {
        val key = getSetting("credential-access-key");
        val secret = getSetting("credential-secret-key");
        val credentials = ChainingAWSCredentialsProvider.getInstance(key, secret);
        builder.credentialsProvider(credentials);

        val region = getSetting("region");
        builder.region(StringUtils.isBlank(region) ? Region.AWS_GLOBAL : Region.of(region));

        val endpoint = getSetting("endpoint");
        if (StringUtils.isNotBlank(endpoint)) {
            builder.endpointOverride(new URI(endpoint));
        }
        val result = builder.build();
        return clientType.cast(result);
    }
}
