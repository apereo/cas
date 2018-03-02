package org.apereo.cas.oidc.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.beans.factory.FactoryBean;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OidcServerDiscoverySettingsFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcServerDiscoverySettingsFactory implements FactoryBean<OidcServerDiscoverySettings> {
    private final CasConfigurationProperties casProperties;

    public OidcServerDiscoverySettingsFactory(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Override
    public OidcServerDiscoverySettings getObject() {
        final OidcProperties oidc = casProperties.getAuthn().getOidc();
        final OidcServerDiscoverySettings discoveryProperties =
                new OidcServerDiscoverySettings(casProperties, oidc.getIssuer());

        discoveryProperties.setClaimsSupported(oidc.getClaims());
        discoveryProperties.setScopesSupported(oidc.getScopes());
        discoveryProperties.setResponseTypesSupported(
                CollectionUtils.wrapList(OAuth20ResponseTypes.CODE.getType(),
                        OAuth20ResponseTypes.TOKEN.getType(),
                        OAuth20ResponseTypes.IDTOKEN_TOKEN.getType()));

        discoveryProperties.setSubjectTypesSupported(oidc.getSubjectTypes());
        discoveryProperties.setClaimTypesSupported(CollectionUtils.wrap("normal"));

        final List<String> authnMethods = new ArrayList<>();
        authnMethods.add("client_secret_basic");
        discoveryProperties.setIntrospectionSupportedAuthenticationMethods(authnMethods);

        discoveryProperties.setGrantTypesSupported(
                CollectionUtils.wrapList(OAuth20GrantTypes.AUTHORIZATION_CODE.getType(),
                        OAuth20GrantTypes.PASSWORD.getType(),
                        OAuth20GrantTypes.CLIENT_CREDENTIALS.getType(),
                        OAuth20GrantTypes.REFRESH_TOKEN.getType()));

        discoveryProperties.setIdTokenSigningAlgValuesSupported(CollectionUtils.wrapList("none", "RS256"));
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
