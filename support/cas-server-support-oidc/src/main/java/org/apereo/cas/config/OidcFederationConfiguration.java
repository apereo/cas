package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.federation.OidcFederationDefaultEntityStatementService;
import org.apereo.cas.oidc.federation.OidcFederationDefaultJsonWebKeystoreService;
import org.apereo.cas.oidc.federation.OidcFederationDefaultTrustChainResolver;
import org.apereo.cas.oidc.federation.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.OidcFederationJsonWebKeystoreService;
import org.apereo.cas.oidc.federation.OidcFederationTrustChainResolver;
import org.apereo.cas.oidc.federation.OidcWellKnownFederationEndpointController;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.stream.Collectors;

/**
 * This is {@link OidcFederationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect, module = "federation", enabledByDefault = false)
@Configuration(value = "OidcFederationConfiguration", proxyBeanMethods = false)
class OidcFederationConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcWellKnownFederationController")
    @Bean
    public OidcWellKnownFederationEndpointController oidcWellKnownFederationController(
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext,
        @Qualifier(OidcFederationEntityStatementService.BEAN_NAME)
        final OidcFederationEntityStatementService oidcFederationEntityStatementService) {
        return new OidcWellKnownFederationEndpointController(oidcConfigurationContext, oidcFederationEntityStatementService);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = OidcFederationJsonWebKeystoreService.BEAN_NAME)
    public OidcFederationJsonWebKeystoreService oidcFederationWebKeystoreService(
        final CasConfigurationProperties casProperties) throws Exception {
        return new OidcFederationDefaultJsonWebKeystoreService(casProperties.getAuthn().getOidc());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = OidcFederationEntityStatementService.BEAN_NAME)
    public OidcFederationEntityStatementService oidcFederationEntityStatementService(
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        final OidcFederationJsonWebKeystoreService oidcFederationWebKeystoreService,
        final CasConfigurationProperties casProperties,
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcFederationDefaultEntityStatementService(
            oidcFederationWebKeystoreService,
            oidcConfigurationContext.getDiscoverySettings(),
            casProperties.getAuthn().getOidc());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = OidcFederationTrustChainResolver.BEAN_NAME)
    public OidcFederationTrustChainResolver oidcFederationTrustChainResolver(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val trustChainResolvers = casProperties.getAuthn().getOidc().getFederation().getAuthorityHints()
            .stream()
            .map(EntityID::new)
            .map(TrustChainResolver::new)
            .collect(Collectors.toList());

        val resolvers = applicationContext.getBeansOfType(TrustChainResolver.class).values();
        trustChainResolvers.addAll(resolvers);

        return new OidcFederationDefaultTrustChainResolver(trustChainResolvers);
    }
}
