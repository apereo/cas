package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.authz.OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.oidc.vc.token.OidcVerifiableCredentialsPreAuthorizationCodeAuthenticator;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.OAuth20AuthenticationClientProvider;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import lombok.val;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.http.client.direct.DirectFormClient;
import org.springframework.beans.factory.ObjectProvider;
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

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OAuth20AuthenticationClientProvider oidcVerifiableCredentialsPreAuthorizationCodeAuthenticationClientProvider(
        @Qualifier("oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator")
        final Authenticator oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator) {
        return () -> {
            val accessTokenClient = new DirectFormClient(oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator);
            accessTokenClient.setName("oidcVerifiableCredentialsPreAuthorizationCodeAuthenticationClientProvider");
            accessTokenClient.setUsernameParameter(OidcConstants.PRE_AUTHORIZED_CODE);
            accessTokenClient.setPasswordParameter(OAuth20Constants.GRANT_TYPE);
            accessTokenClient.init();
            return accessTokenClient;
        };
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator")
    public Authenticator oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator(
        @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
        final OAuth20RequestParameterResolver oauthRequestParameterResolver,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver principalResolver,
        @Qualifier(OidcVerifiableCredentialTransactionService.BEAN_NAME)
        final ObjectProvider<OidcVerifiableCredentialTransactionService> oidcVerifiableCredentialTransactionService) {
        return new OidcVerifiableCredentialsPreAuthorizationCodeAuthenticator(
            oidcVerifiableCredentialTransactionService, principalResolver, oauthRequestParameterResolver);
    }
}
