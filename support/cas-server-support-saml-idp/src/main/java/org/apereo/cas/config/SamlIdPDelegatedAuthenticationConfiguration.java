package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.web.idp.delegation.SamlIdPDelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnFeaturesEnabled;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SamlIdPDelegatedAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(DelegatedAuthenticationWebflowConfiguration.class)
@ConditionalOnFeaturesEnabled({
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication),
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProvider)
})
@Configuration(value = "SamlIdPDelegatedAuthenticationConfiguration", proxyBeanMethods = false)
class SamlIdPDelegatedAuthenticationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "saml2DelegatedClientAuthenticationRequestCustomizer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnClass(SAML2Client.class)
    public DelegatedClientAuthenticationRequestCustomizer saml2DelegatedClientAuthenticationRequestCustomizer(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier("samlIdPDistributedSessionStore")
        final SessionStore sessionStore,
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        final OpenSamlConfigBean openSamlConfigBean) {
        return new SamlIdPDelegatedClientAuthenticationRequestCustomizer(
            sessionStore, openSamlConfigBean, servicesManager, casProperties);
    }
}
