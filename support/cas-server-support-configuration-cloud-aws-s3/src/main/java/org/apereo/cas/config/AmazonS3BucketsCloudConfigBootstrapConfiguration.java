package org.apereo.cas.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
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
import java.util.List;
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
            final AmazonEnvironmentAwareClientBuilder builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
            final AmazonS3 s3Client = builder.build(AmazonS3ClientBuilder.standard(), AmazonS3.class);

            final String bucketName = builder.getSetting("bucketName", "cas-properties");
            LOGGER.debug("Locating S3 object(s) from bucket [{}]...", bucketName);
            final ListObjectsV2Result result = s3Client.listObjectsV2(bucketName);
            final List<S3ObjectSummary> objects = result.getObjectSummaries();
            LOGGER.debug("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucketName);

            objects.forEach(obj -> {
                final String objectKey = obj.getKey();
                LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucketName);
                final S3Object object = s3Client.getObject(obj.getBucketName(), objectKey);
                try (S3ObjectInputStream is = object.getObjectContent()) {
                    if (objectKey.endsWith("properties")) {
                        final Properties props = new Properties();
                        props.load(is);
                        props.entrySet().forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
                    } else if (objectKey.endsWith("yml")) {
                        final Map yamlProps = CasCoreConfigurationUtils.loadYamlProperties(new InputStreamResource(is));
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
