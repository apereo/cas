package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.RestSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPRestfulMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("samlIdPRestMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPRestfulMetadataConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Bean
    @ConditionalOnMissingBean(name = "restSamlRegisteredServiceMetadataResolver")
    @RefreshScope
    public SamlRegisteredServiceMetadataResolver restSamlRegisteredServiceMetadataResolver() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new RestSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "restSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer restSamlRegisteredServiceMetadataResolutionPlanConfigurer() {
        return plan -> plan.registerMetadataResolver(restSamlRegisteredServiceMetadataResolver());
    }
}
