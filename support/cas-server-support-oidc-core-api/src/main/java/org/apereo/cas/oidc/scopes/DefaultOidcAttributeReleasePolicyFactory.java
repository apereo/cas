package org.apereo.cas.oidc.scopes;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcAddressScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcOpenIdScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcPhoneScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicy;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultOidcAttributeReleasePolicyFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class DefaultOidcAttributeReleasePolicyFactory implements OidcAttributeReleasePolicyFactory {
    private final CasConfigurationProperties casProperties;

    @Override
    public BaseOidcScopeAttributeReleasePolicy get(final OidcConstants.StandardScopes scope) {
        switch (scope) {
            case EMAIL:
                return new OidcEmailScopeAttributeReleasePolicy();
            case ADDRESS:
                return new OidcAddressScopeAttributeReleasePolicy();
            case OPENID:
                return new OidcOpenIdScopeAttributeReleasePolicy();
            case PHONE:
                return new OidcPhoneScopeAttributeReleasePolicy();
            case PROFILE:
                return new OidcProfileScopeAttributeReleasePolicy();
            default:
                return null;
        }
    }

    @Override
    public OidcCustomScopeAttributeReleasePolicy custom(final String name, final List<String> allowedAttributes) {
        return new OidcCustomScopeAttributeReleasePolicy(name, allowedAttributes);
    }

    @Override
    public Collection<OidcCustomScopeAttributeReleasePolicy> getUserDefinedScopes() {
        return from(casProperties.getAuthn().getOidc().getCore().getUserDefinedScopes());
    }
}
