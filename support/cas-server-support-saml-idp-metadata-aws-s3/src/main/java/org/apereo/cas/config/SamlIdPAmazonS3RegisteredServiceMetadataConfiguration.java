package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.AmazonS3SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
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
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLServiceProviderMetadata, module = "aws-s3")
@Configuration(value = "SamlIdPAmazonS3RegisteredServiceMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPAmazonS3RegisteredServiceMetadataConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.saml-idp.metadata.amazon-s3.bucket-name");

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlRegisteredServiceMetadataResolver amazonS3SamlRegisteredServiceMetadataResolver(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        final OpenSamlConfigBean openSamlConfigBean,
        @Qualifier("amazonS3Client")
        final S3Client amazonS3Client) {
        return BeanSupplier.of(SamlRegisteredServiceMetadataResolver.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                return new AmazonS3SamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean, amazonS3Client);
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amazonS3SamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer amazonS3SamlRegisteredServiceMetadataResolutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("amazonS3SamlRegisteredServiceMetadataResolver")
        final SamlRegisteredServiceMetadataResolver amazonS3SamlRegisteredServiceMetadataResolver) {
        return BeanSupplier.of(SamlRegisteredServiceMetadataResolutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerMetadataResolver(amazonS3SamlRegisteredServiceMetadataResolver))
            .otherwiseProxy()
            .get();
    }
}
