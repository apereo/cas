package org.apereo.cas.config;

import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public AmazonS3 amazonS3Client() {
        val amz = casProperties.getAuthn().getSamlIdp().getMetadata().getAmazonS3();
        val endpoint = new AwsClientBuilder.EndpointConfiguration(amz.getEndpoint(), amz.getRegion());
        val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(),
            amz.getCredentialsPropertiesFile(),
            amz.getProfilePath(),
            amz.getProfileName());

        val builder = AmazonS3ClientBuilder
            .standard()
            .withCredentials(credentials)
            .withEndpointConfiguration(endpoint);

        if (StringUtils.isNotBlank(amz.getRegion())) {
            builder.withRegion(amz.getRegion());
        }
        return builder.build();
    }
}
