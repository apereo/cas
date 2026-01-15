package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * This is {@link CasAmazonS3BucketsCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "aws-s3")
@AutoConfiguration
public class CasAmazonS3BucketsCloudConfigBootstrapAutoConfiguration {
    /**
     * Amazon S3 CAS configuration key prefix.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.aws.s3";

    @Bean
    @ConditionalOnMissingBean(name = "s3BucketPropertySourceLocator")
    public PropertySourceLocator amazonS3BucketPropertySourceLocator(final Environment environment) {
        val builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
        val clientBuilder = S3Client.builder().serviceConfiguration(S3Configuration.Builder::pathStyleAccessEnabled)
            .forcePathStyle(true);
        val s3Client = builder.build(clientBuilder, S3Client.class);
        val bucketName = builder.getSetting("bucket-name", "cas-properties");
        return new AmazonS3PropertySourceLocator(s3Client, bucketName);
    }
}
