package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.AmazonS3SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * This is {@link SamlIdPAmazonS3RegisteredServiceMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "samlIdPAmazonS3RegisteredServiceMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPAmazonS3RegisteredServiceMetadataConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public SamlRegisteredServiceMetadataResolver amazonS3SamlRegisteredServiceMetadataResolver(final CasConfigurationProperties casProperties,
                                                                                               @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
                                                                                               final OpenSamlConfigBean openSamlConfigBean,
                                                                                               @Qualifier("amazonS3Client")
                                                                                               final S3Client amazonS3Client) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new AmazonS3SamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean, amazonS3Client);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amazonS3SamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer amazonS3SamlRegisteredServiceMetadataResolutionPlanConfigurer(
        @Qualifier("amazonS3SamlRegisteredServiceMetadataResolver")
        final SamlRegisteredServiceMetadataResolver amazonS3SamlRegisteredServiceMetadataResolver) {
        return plan -> plan.registerMetadataResolver(amazonS3SamlRegisteredServiceMetadataResolver);
    }
}
