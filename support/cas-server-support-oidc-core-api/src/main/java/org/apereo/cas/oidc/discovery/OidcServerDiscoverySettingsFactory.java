package org.apereo.cas.oidc.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.FactoryBean;

/**
 * This is {@link OidcServerDiscoverySettingsFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class OidcServerDiscoverySettingsFactory implements FactoryBean<OidcServerDiscoverySettings> {
    private final CasConfigurationProperties casProperties;

    @Override
    public OidcServerDiscoverySettings getObject() {
        val oidc = casProperties.getAuthn().getOidc();
        val discovery = new OidcServerDiscoverySettings(casProperties, oidc.getIssuer());
        discovery.setClaimsSupported(oidc.getClaims());
        discovery.setScopesSupported(oidc.getScopes());
        discovery.setResponseTypesSupported(oidc.getResponseTypesSupported());
        discovery.setSubjectTypesSupported(oidc.getSubjectTypes());
        discovery.setClaimTypesSupported(oidc.getClaimTypesSupported());
        discovery.setIntrospectionSupportedAuthenticationMethods(oidc.getIntrospectionSupportedAuthenticationMethods());
        discovery.setGrantTypesSupported(oidc.getGrantTypesSupported());
        discovery.setTokenEndpointAuthMethodsSupported(oidc.getTokenEndpointAuthMethodsSupported());
        discovery.setClaimsParameterSupported(true);

        discovery.setIdTokenSigningAlgValuesSupported(oidc.getIdTokenSigningAlgValuesSupported());
        discovery.setIdTokenEncryptionAlgValuesSupported(oidc.getIdTokenEncryptionAlgValuesSupported());
        discovery.setIdTokenEncryptionEncodingValuesSupported(oidc.getIdTokenEncryptionEncodingValuesSupported());

        discovery.setBackchannelLogoutSupported(oidc.getLogout().isBackchannelLogoutSupported());
        discovery.setFrontchannelLogoutSupported(oidc.getLogout().isFrontchannelLogoutSupported());

        discovery.setUserInfoSigningAlgValuesSupported(oidc.getUserInfoSigningAlgValuesSupported());
        discovery.setUserInfoEncryptionAlgValuesSupported(oidc.getUserInfoEncryptionAlgValuesSupported());
        discovery.setUserInfoEncryptionEncodingValuesSupported(oidc.getUserInfoEncryptionEncodingValuesSupported());
        discovery.setCodeChallengeMethodsSupported(oidc.getCodeChallengeMethodsSupported());

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
