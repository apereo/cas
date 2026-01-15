package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * This is {@link AmazonS3SamlMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAML, module = "aws-s3")
@Configuration(value = "AmazonS3SamlMetadataConfiguration", proxyBeanMethods = false)
class AmazonS3SamlMetadataConfiguration {

    @ConditionalOnMissingBean(name = "amazonS3Client")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public S3Client amazonS3Client(final CasConfigurationProperties casProperties) {
        val amz = casProperties.getAuthn().getSamlIdp().getMetadata().getAmazonS3();
        val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(), amz.getProfilePath(), amz.getProfileName());
        val builder = S3Client.builder();
        AmazonClientConfigurationBuilder.prepareSyncClientBuilder(builder, credentials, amz);
        return builder.build();
    }
}
