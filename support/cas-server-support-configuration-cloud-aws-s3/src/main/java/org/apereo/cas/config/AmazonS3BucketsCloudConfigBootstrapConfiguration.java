package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.InputStreamResource;

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
            val s3Client = builder.build(AmazonS3ClientBuilder.standard(), AmazonS3.class);

            val bucketName = builder.getSetting("bucketName", "cas-properties");
            LOGGER.debug("Locating S3 object(s) from bucket [{}]...", bucketName);
            val result = s3Client.listObjectsV2(bucketName);
            val objects = result.getObjectSummaries();
            LOGGER.debug("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucketName);

            objects.forEach(obj -> {
                val objectKey = obj.getKey();
                LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucketName);
                val object = s3Client.getObject(obj.getBucketName(), objectKey);
                try (val is = object.getObjectContent()) {
                    if (objectKey.endsWith("properties")) {
                        val props = new Properties();
                        props.load(is);
                        props.forEach((key, value) -> properties.put(key.toString(), value));
                    } else if (objectKey.endsWith("yml") || objectKey.endsWith("yaml")) {
                        val yamlProps = CasCoreConfigurationUtils.loadYamlProperties(new InputStreamResource(is));
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
