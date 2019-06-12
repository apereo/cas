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
        val discoveryProperties = new OidcServerDiscoverySettings(casProperties, oidc.getIssuer());
        discoveryProperties.setClaimsSupported(oidc.getClaims());
        discoveryProperties.setScopesSupported(oidc.getScopes());
        discoveryProperties.setResponseTypesSupported(oidc.getResponseTypesSupported());
        discoveryProperties.setSubjectTypesSupported(oidc.getSubjectTypes());
        discoveryProperties.setClaimTypesSupported(oidc.getClaimTypesSupported());
        discoveryProperties.setIntrospectionSupportedAuthenticationMethods(oidc.getIntrospectionSupportedAuthenticationMethods());
        discoveryProperties.setGrantTypesSupported(oidc.getGrantTypesSupported());
        discoveryProperties.setTokenEndpointAuthMethodsSupported(oidc.getTokenEndpointAuthMethodsSupported());
        discoveryProperties.setClaimsParameterSupported(true);

        discoveryProperties.setIdTokenSigningAlgValuesSupported(oidc.getIdTokenSigningAlgValuesSupported());
        discoveryProperties.setIdTokenEncryptionAlgValuesSupported(oidc.getIdTokenEncryptionAlgValuesSupported());
        discoveryProperties.setIdTokenEncryptionEncodingValuesSupported(oidc.getIdTokenEncryptionEncodingValuesSupported());

        discoveryProperties.setUserInfoSigningAlgValuesSupported(oidc.getUserInfoSigningAlgValuesSupported());
        discoveryProperties.setUserInfoEncryptionAlgValuesSupported(oidc.getUserInfoEncryptionAlgValuesSupported());
        discoveryProperties.setUserInfoEncryptionEncodingValuesSupported(oidc.getUserInfoEncryptionEncodingValuesSupported());
        
        return discoveryProperties;
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
