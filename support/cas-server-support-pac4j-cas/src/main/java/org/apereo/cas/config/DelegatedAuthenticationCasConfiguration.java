package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.pac4j.web.DelegatedClientCasBuilder;
import org.apereo.cas.pac4j.web.DelegatedClientsCasEndpointContributor;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpointContributor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DelegatedAuthenticationCasConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "cas")
@Configuration(value = "DelegatedAuthenticationCasConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatedAuthenticationCasConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedClientsCasEndpointContributor")
    public DelegatedClientsEndpointContributor delegatedClientsCasEndpointContributor() {
        return new DelegatedClientsCasEndpointContributor();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedCasClientBuilder")
    public ConfigurableDelegatedClientBuilder delegatedCasClientBuilder(
        @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext) {
        return new DelegatedClientCasBuilder(casSslContext);
    }
}
