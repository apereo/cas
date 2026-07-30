package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.vc.authz.OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link OidcVerifiableCredentialsAuthorizationCodeConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "OidcVerifiableCredentialsAuthorizationCodeConfiguration", proxyBeanMethods = false)
class OidcVerifiableCredentialsAuthorizationCodeConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialAuthorizationCodeResponseBuilder")
    public OAuth20AuthorizationResponseBuilder oidcVerifiableCredentialAuthorizationCodeResponseBuilder(
        @Qualifier("oauthAuthorizationModelAndViewBuilder")
        final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder(
            oidcConfigurationContext, oauthAuthorizationModelAndViewBuilder);
    }
}
