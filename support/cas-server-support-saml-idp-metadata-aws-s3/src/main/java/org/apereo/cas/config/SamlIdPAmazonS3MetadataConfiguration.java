package org.apereo.cas.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.AmazonS3SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * This is {@link SamlIdPAmazonS3MetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("samlIdPAmazonS3MetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SamlIdPAmazonS3MetadataConfiguration implements SamlRegisteredServiceMetadataResolutionPlanConfigurator {

    @Autowired
    private Environment environment;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Bean
    public SamlRegisteredServiceMetadataResolver amazonS3SamlRegisteredServiceMetadataResolver() {
        final var idp = casProperties.getAuthn().getSamlIdp();
        return new AmazonS3SamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean, amazonS3Client());
    }

    @ConditionalOnMissingBean(name = "amazonS3Client")
    @Bean
    @RefreshScope
    public AmazonS3 amazonS3Client() {
        final var amz = casProperties.getAuthn().getSamlIdp().getMetadata().getAmazonS3();
        final var endpoint = new AwsClientBuilder.EndpointConfiguration(amz.getEndpoint(), amz.getRegion());
        final var credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(),
            amz.getCredentialsPropertiesFile(),
            amz.getProfilePath(),
            amz.getProfileName());
        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(credentials)
            .withRegion(amz.getRegion())
            .withEndpointConfiguration(endpoint)
            .build();
    }

    @Override
    public void configureMetadataResolutionPlan(final SamlRegisteredServiceMetadataResolutionPlan plan) {
        plan.registerMetadataResolver(amazonS3SamlRegisteredServiceMetadataResolver());
    }
}
