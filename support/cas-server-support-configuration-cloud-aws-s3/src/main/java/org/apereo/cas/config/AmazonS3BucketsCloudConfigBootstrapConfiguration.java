package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.InputStreamResource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * This is {@link AmazonS3BucketsCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "AmazonS3BucketsCloudConfigBootstrapConfiguration", proxyBeanMethods = false)
@Slf4j
@Getter
public class AmazonS3BucketsCloudConfigBootstrapConfiguration implements PropertySourceLocator {
    /**
     * Amazon S3 CAS configuration key prefix.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.aws.s3";

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val properties = new LinkedHashMap<String, Object>();
        try {
            val builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
            val s3Client = builder.build(S3Client.builder(), S3Client.class);

            val bucketName = builder.getSetting("bucket-name", "cas-properties");
            LOGGER.debug("Locating S3 object(s) from bucket [{}]...", bucketName);
            val result = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
            val objects = result.contents();
            LOGGER.debug("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucketName);

            objects.forEach(obj -> {
                val objectKey = obj.key();
                LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucketName);
                try (val is = s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(objectKey).build())) {
                    if (objectKey.endsWith("properties")) {
                        val props = new Properties();
                        props.load(is);
                        props.forEach((key, value) -> properties.put(key.toString(), value));
                    } else if (objectKey.endsWith("yml") || objectKey.endsWith("yaml")) {
                        val yamlProps = CasCoreConfigurationUtils.loadYamlProperties(new InputStreamResource(is));
                        properties.putAll(yamlProps);
                    }
                } catch (final Exception e) {
                    LoggingUtils.error(LOGGER, e);
                }
            });
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new MapPropertySource(getClass().getSimpleName(), properties);
    }
}
