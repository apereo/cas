package org.apereo.cas.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.InputStreamResource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link AmazonS3BucketsCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("AmazonS3BucketsCloudConfigBootstrapConfiguration")
@Slf4j
@Getter
public class AmazonS3BucketsCloudConfigBootstrapConfiguration implements PropertySourceLocator {
    private static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.aws.s3";

    @Override
    public PropertySource<?> locate(final Environment environment) {
        final Map properties = new LinkedHashMap<>();
        try {
            final var builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
            final var s3Client = builder.build(AmazonS3ClientBuilder.standard(), AmazonS3.class);

            final var bucketName = builder.getSetting("bucketName", "cas-properties");
            LOGGER.debug("Locating S3 object(s) from bucket [{}]...", bucketName);
            final var result = s3Client.listObjectsV2(bucketName);
            final var objects = result.getObjectSummaries();
            LOGGER.debug("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucketName);

            objects.forEach(obj -> {
                final var objectKey = obj.getKey();
                LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucketName);
                final var object = s3Client.getObject(obj.getBucketName(), objectKey);
                try (var is = object.getObjectContent()) {
                    if (objectKey.endsWith("properties")) {
                        final var props = new Properties();
                        props.load(is);
                        props.entrySet().forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
                    } else if (objectKey.endsWith("yml")) {
                        final var yamlProps = CasCoreConfigurationUtils.loadYamlProperties(new InputStreamResource(is));
                        properties.putAll(yamlProps);
                    }
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new MapPropertySource(getClass().getSimpleName(), properties);
    }
}
