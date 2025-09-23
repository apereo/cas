package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.metadata.s3.SAML2S3MetadataGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * This is {@link DelegatedAuthenticationSaml2AmazonS3Configuration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */

@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml-s3", enabledByDefault = false)
@Configuration(value = "DelegatedAuthenticationSaml2AmazonS3Configuration", proxyBeanMethods = false)
@ConditionalOnClass(S3Client.class)
class DelegatedAuthenticationSaml2AmazonS3Configuration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedSaml2ClientAmazonS3MetadataCustomizer")
    public DelegatedClientFactoryCustomizer delegatedSaml2ClientAmazonS3MetadataCustomizer(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext) {
        return client -> {
            if (client instanceof final SAML2Client saml2Client) {
                val configuration = saml2Client.getConfiguration();
                casProperties.getAuthn().getPac4j().getSaml().forEach(saml -> {
                    val amz = saml.getMetadata().getServiceProvider().getAmazonS3();
                    val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
                        amz.getCredentialSecretKey(), amz.getProfilePath(), amz.getProfileName());
                    val builder = S3Client.builder();
                    AmazonClientConfigurationBuilder.prepareSyncClientBuilder(builder, credentials, amz);
                    val s3Client = builder.build();
                    val metadataGenerator = new SAML2S3MetadataGenerator(s3Client, configuration.getServiceProviderEntityId());
                    
                    configuration.setServiceProviderMetadataResource(ResourceUtils.NULL_RESOURCE);
                    configuration.setMetadataGenerator(metadataGenerator);
                });
            }
        };
    }
}
