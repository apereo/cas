package org.apereo.cas.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

/**
 * This is {@link AmazonEnvironmentAwareClientBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
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
    public <T> T build(final AwsClientBuilder builder, final Class<T> clientType) {
        val cfg = new ClientConfiguration();
        try {
            val localAddress = getSetting("localAddress");
            if (StringUtils.isNotBlank(localAddress)) {
                cfg.setLocalAddress(InetAddress.getByName(localAddress));
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        builder.withClientConfiguration(cfg);

        val key = getSetting("credentialAccessKey");
        val secret = getSetting("credentialSecretKey");
        val credentials = ChainingAWSCredentialsProvider.getInstance(key, secret);
        builder.withCredentials(credentials);

        var region = getSetting("region");
        val currentRegion = Regions.getCurrentRegion();
        if (currentRegion != null && StringUtils.isBlank(region)) {
            region = currentRegion.getName();
        }
        var regionOverride = getSetting("regionOverride");
        if (currentRegion != null && StringUtils.isNotBlank(regionOverride)) {
            regionOverride = currentRegion.getName();
        }
        val finalRegion = StringUtils.defaultIfBlank(regionOverride, region);
        if (StringUtils.isNotBlank(finalRegion)) {
            builder.withRegion(finalRegion);
        }

        val endpoint = getSetting("endpoint");
        if (StringUtils.isNotBlank(endpoint)) {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, finalRegion));
        }

        val result = builder.build();
        return clientType.cast(result);
    }
}
