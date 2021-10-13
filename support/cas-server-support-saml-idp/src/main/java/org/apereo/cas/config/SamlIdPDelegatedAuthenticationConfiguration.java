package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.web.idp.delegation.SamlIdPDelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.web.flow.config.DelegatedAuthenticationWebflowConfiguration;

import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPDelegatedAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "samlIdPDelegatedAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(DelegatedAuthenticationWebflowConfiguration.class)
public class SamlIdPDelegatedAuthenticationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "saml2DelegatedClientAuthenticationRequestCustomizer")
    public DelegatedClientAuthenticationRequestCustomizer saml2DelegatedClientAuthenticationRequestCustomizer(
        @Qualifier(DistributedJEESessionStore.DEFAULT_BEAN_NAME)
        final SessionStore sessionStore,
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        final OpenSamlConfigBean openSamlConfigBean) {
        return new SamlIdPDelegatedClientAuthenticationRequestCustomizer(sessionStore, openSamlConfigBean);
    }
}
