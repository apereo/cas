package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.federation.OidcFederationDefaultEntityStatementService;
import org.apereo.cas.oidc.federation.OidcFederationDefaultJsonWebKeystoreService;
import org.apereo.cas.oidc.federation.OidcFederationDefaultTrustChainResolver;
import org.apereo.cas.oidc.federation.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.OidcFederationJsonWebKeystoreService;
import org.apereo.cas.oidc.federation.OidcFederationTrustChainResolver;
import org.apereo.cas.oidc.federation.OidcWellKnownFederationEndpointController;
import org.apereo.cas.oidc.issuer.OidcDefaultIssuerService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.apereo.cas.oidc.OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL;

/**
 * This is {@link OidcFederationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Jerome LELEU
 * @since 7.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect, module = "federation", enabledByDefault = false)
@Configuration(value = "OidcFederationConfiguration", proxyBeanMethods = false)
class OidcFederationConfiguration {

    private static final String OIDC_FEDERATION_ISSUER_SERVICE = "oidcFederationIssuerService";

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = OIDC_FEDERATION_ISSUER_SERVICE)
    public OidcIssuerService oidcFederationIssuerService(
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        final CasConfigurationProperties casProperties) {
        return new OidcDefaultIssuerService(casProperties.getAuthn().getOidc(), tenantExtractor);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcWellKnownFederationController")
    @Bean
    public OidcWellKnownFederationEndpointController oidcWellKnownFederationController(
        @Qualifier(OIDC_FEDERATION_ISSUER_SERVICE)
        final OidcIssuerService oidcFederationIssuerService,
        @Qualifier(OidcFederationEntityStatementService.BEAN_NAME)
        final OidcFederationEntityStatementService oidcFederationEntityStatementService) {
        return new OidcWellKnownFederationEndpointController(oidcFederationIssuerService, oidcFederationEntityStatementService);
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
        @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
        final ObjectProvider<OidcServerDiscoverySettings> oidcServerDiscoverySettings) {
        return new OidcFederationDefaultEntityStatementService(
            oidcFederationWebKeystoreService,
            oidcServerDiscoverySettings,
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

    @Bean
    @ConditionalOnMissingBean(name = "oidcFederationProtocolEndpointConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebSecurityConfigurer<HttpSecurity> oidcFederationProtocolEndpointConfigurer(
        @Qualifier(OIDC_FEDERATION_ISSUER_SERVICE)
        final OidcIssuerService oidcIssuerService,
        final CasConfigurationProperties casProperties) {
        val baseEndpoint = getOidcBaseEndpoint(oidcIssuerService, casProperties);
        return new CasWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                return List.of(baseEndpoint + WELL_KNOWN_OPENID_FEDERATION_URL.substring(1));
            }
        };
    }

    private static String getOidcBaseEndpoint(final OidcIssuerService issuerService,
                                              final CasConfigurationProperties casProperties) {
        val issuer = issuerService.determineIssuer(Optional.empty());
        val endpoint = Strings.CI.remove(issuer, casProperties.getServer().getPrefix());
        return Strings.CI.prependIfMissing(endpoint, "/");
    }
}
