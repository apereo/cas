package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialDefaultOfferService;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialDefaultTransactionService;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialOfferService;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.oidc.vc.offer.web.OidcVerifiableCredentialOfferEndpointController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link OidcVerifiableCredentialsOfferConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "OidcVerifiableCredentialsOfferConfiguration", proxyBeanMethods = false)
class OidcVerifiableCredentialsOfferConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = OidcVerifiableCredentialTransactionService.BEAN_NAME)
    public OidcVerifiableCredentialTransactionService oidcVerifiableCredentialTransactionService(
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialDefaultTransactionService(oidcConfigurationContext);
    }


    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = OidcVerifiableCredentialOfferService.BEAN_NAME)
    public OidcVerifiableCredentialOfferService oidcVerifiableCredentialCredentialOfferService(
        @Qualifier(OidcVerifiableCredentialTransactionService.BEAN_NAME)
        final OidcVerifiableCredentialTransactionService oidcVerifiableCredentialTransactionService,
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialDefaultOfferService(
            oidcConfigurationContext, oidcVerifiableCredentialTransactionService);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialOfferEndpointController")
    public OidcVerifiableCredentialOfferEndpointController oidcVerifiableCredentialOfferEndpointController(
        @Qualifier(OidcVerifiableCredentialOfferService.BEAN_NAME)
        final OidcVerifiableCredentialOfferService oidcVcCredentialOfferService,
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialOfferEndpointController(
            oidcConfigurationContext, oidcVcCredentialOfferService);
    }
}
