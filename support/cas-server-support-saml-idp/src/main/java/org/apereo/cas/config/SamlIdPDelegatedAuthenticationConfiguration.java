package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.web.idp.delegation.SamlIdPDelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnFeaturesEnabled;

import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
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
@AutoConfiguration
public class SamlIdPDelegatedAuthenticationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "saml2DelegatedClientAuthenticationRequestCustomizer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedClientAuthenticationRequestCustomizer saml2DelegatedClientAuthenticationRequestCustomizer(
        final CasConfigurationProperties casProperties,
        @Qualifier(DistributedJEESessionStore.DEFAULT_BEAN_NAME)
        final SessionStore sessionStore,
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        final OpenSamlConfigBean openSamlConfigBean) {
        return new SamlIdPDelegatedClientAuthenticationRequestCustomizer(sessionStore, openSamlConfigBean, casProperties);
    }
}
