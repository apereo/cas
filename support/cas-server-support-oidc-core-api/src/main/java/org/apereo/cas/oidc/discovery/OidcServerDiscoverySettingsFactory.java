package org.apereo.cas.oidc.discovery;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.issuer.OidcIssuerService;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link OidcServerDiscoverySettingsFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class OidcServerDiscoverySettingsFactory implements FactoryBean<OidcServerDiscoverySettings> {
    private final CasConfigurationProperties casProperties;

    private final OidcIssuerService issuerService;

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public OidcServerDiscoverySettings getObject() {
        val oidc = casProperties.getAuthn().getOidc();
        val discoveryConfig = oidc.getDiscovery();

        val discovery = new OidcServerDiscoverySettings(issuerService.determineIssuer(Optional.empty()));

        discovery.setClaimsSupported(discoveryConfig.getClaims());
        discovery.setScopesSupported(discoveryConfig.getScopes());
        discovery.setResponseTypesSupported(discoveryConfig.getResponseTypesSupported());
        discovery.setSubjectTypesSupported(discoveryConfig.getSubjectTypes());
        discovery.setClaimTypesSupported(discoveryConfig.getClaimTypesSupported());
        discovery.setIntrospectionSupportedAuthenticationMethods(discoveryConfig.getIntrospectionSupportedAuthenticationMethods());
        discovery.setGrantTypesSupported(discoveryConfig.getGrantTypesSupported());
        discovery.setTokenEndpointAuthMethodsSupported(discoveryConfig.getTokenEndpointAuthMethodsSupported());
        discovery.setClaimsParameterSupported(discoveryConfig.isClaimsParameterSupported());

        discovery.setIdTokenSigningAlgValuesSupported(discoveryConfig.getIdTokenSigningAlgValuesSupported());
        discovery.setIdTokenEncryptionAlgValuesSupported(discoveryConfig.getIdTokenEncryptionAlgValuesSupported());
        discovery.setIdTokenEncryptionEncodingValuesSupported(discoveryConfig.getIdTokenEncryptionEncodingValuesSupported());

        discovery.setBackchannelLogoutSupported(oidc.getLogout().isBackchannelLogoutSupported());
        discovery.setFrontchannelLogoutSupported(oidc.getLogout().isFrontchannelLogoutSupported());

        discovery.setUserInfoSigningAlgValuesSupported(discoveryConfig.getUserInfoSigningAlgValuesSupported());
        discovery.setUserInfoEncryptionAlgValuesSupported(discoveryConfig.getUserInfoEncryptionAlgValuesSupported());
        discovery.setUserInfoEncryptionEncodingValuesSupported(discoveryConfig.getUserInfoEncryptionEncodingValuesSupported());

        discovery.setCodeChallengeMethodsSupported(discoveryConfig.getCodeChallengeMethodsSupported());

        discovery.setRequestParameterSupported(discoveryConfig.isRequestParameterSupported());
        discovery.setRequestUriParameterSupported(discoveryConfig.isRequestUriParameterSupported());
        discovery.setRequestObjectSigningAlgValuesSupported(discoveryConfig.getRequestObjectSigningAlgValuesSupported());
        discovery.setRequestObjectEncryptionAlgValuesSupported(discoveryConfig.getRequestObjectEncryptionAlgValuesSupported());
        discovery.setRequestObjectEncryptionEncodingValuesSupported(discoveryConfig.getRequestObjectEncryptionEncodingValuesSupported());
        discovery.setAuthorizationResponseIssuerParameterSupported(discoveryConfig.isAuthorizationResponseIssuerParameterSupported());
        discovery.setAcrValuesSupported(discoveryConfig.getAcrValuesSupported());
        if (discoveryConfig.getAcrValuesSupported().isEmpty()) {
            val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).values()
                .stream()
                .map(MultifactorAuthenticationProvider::getId)
                .collect(Collectors.toList());
            discovery.setAcrValuesSupported(providers);
        }
        return discovery;
    }

    @Override
    public Class<?> getObjectType() {
        return OidcServerDiscoverySettings.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
