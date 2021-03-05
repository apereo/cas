package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * This is {@link AmazonS3SamlMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "amazonS3SamlMetadataConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AmazonS3SamlMetadataConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "amazonS3Client")
    @Bean
    @RefreshScope
    public S3Client amazonS3Client() {
        val amz = casProperties.getAuthn().getSamlIdp().getMetadata().getAmazonS3();
        val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(),
            amz.getProfilePath(),
            amz.getProfileName());
        val builder = S3Client.builder();
        AmazonClientConfigurationBuilder.prepareClientBuilder(builder, credentials, amz);
        return builder.build();
    }
}
