package org.apereo.cas.oidc.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.issuer.OidcIssuerService;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.FactoryBean;

import java.util.Optional;

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

    @Override
    public OidcServerDiscoverySettings getObject() {
        val oidc = casProperties.getAuthn().getOidc();
        val discovery = new OidcServerDiscoverySettings(issuerService.determineIssuer(Optional.empty()));
        discovery.setClaimsSupported(oidc.getDiscovery().getClaims());
        discovery.setScopesSupported(oidc.getDiscovery().getScopes());
        discovery.setResponseTypesSupported(oidc.getDiscovery().getResponseTypesSupported());
        discovery.setSubjectTypesSupported(oidc.getDiscovery().getSubjectTypes());
        discovery.setClaimTypesSupported(oidc.getDiscovery().getClaimTypesSupported());
        discovery.setIntrospectionSupportedAuthenticationMethods(oidc.getDiscovery().getIntrospectionSupportedAuthenticationMethods());
        discovery.setGrantTypesSupported(oidc.getDiscovery().getGrantTypesSupported());
        discovery.setTokenEndpointAuthMethodsSupported(oidc.getDiscovery().getTokenEndpointAuthMethodsSupported());
        discovery.setClaimsParameterSupported(true);

        discovery.setIdTokenSigningAlgValuesSupported(oidc.getDiscovery().getIdTokenSigningAlgValuesSupported());
        discovery.setIdTokenEncryptionAlgValuesSupported(oidc.getDiscovery().getIdTokenEncryptionAlgValuesSupported());
        discovery.setIdTokenEncryptionEncodingValuesSupported(oidc.getDiscovery().getIdTokenEncryptionEncodingValuesSupported());

        discovery.setBackchannelLogoutSupported(oidc.getLogout().isBackchannelLogoutSupported());
        discovery.setFrontchannelLogoutSupported(oidc.getLogout().isFrontchannelLogoutSupported());

        discovery.setUserInfoSigningAlgValuesSupported(oidc.getDiscovery().getUserInfoSigningAlgValuesSupported());
        discovery.setUserInfoEncryptionAlgValuesSupported(oidc.getDiscovery().getUserInfoEncryptionAlgValuesSupported());
        discovery.setUserInfoEncryptionEncodingValuesSupported(oidc.getDiscovery().getUserInfoEncryptionEncodingValuesSupported());
        discovery.setCodeChallengeMethodsSupported(oidc.getDiscovery().getCodeChallengeMethodsSupported());

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
