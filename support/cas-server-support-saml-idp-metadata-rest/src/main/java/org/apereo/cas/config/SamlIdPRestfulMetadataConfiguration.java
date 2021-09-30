package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.RestfulSamlRegisteredServiceMetadataResolver;
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

/**
 * This is {@link SamlIdPRestfulMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "samlIdPRestMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPRestfulMetadataConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "restSamlRegisteredServiceMetadataResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public SamlRegisteredServiceMetadataResolver restSamlRegisteredServiceMetadataResolver(final CasConfigurationProperties casProperties,
                                                                                           @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
                                                                                           final OpenSamlConfigBean openSamlConfigBean) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new RestfulSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "restSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer restSamlRegisteredServiceMetadataResolutionPlanConfigurer(
        @Qualifier("restSamlRegisteredServiceMetadataResolver")
        final SamlRegisteredServiceMetadataResolver restSamlRegisteredServiceMetadataResolver) {
        return plan -> plan.registerMetadataResolver(restSamlRegisteredServiceMetadataResolver);
    }
}
