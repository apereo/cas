package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.OidcConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OidcCustomScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcCustomScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    private static final long serialVersionUID = -8338967628001071540L;

    public OidcCustomScopeAttributeReleasePolicy() {
        this(new ArrayList<>());
    }

    public OidcCustomScopeAttributeReleasePolicy(final List<String> allowedAttributes) {
        this(OidcConstants.StandardScopes.CUSTOM.getScope(), allowedAttributes);
    }

    public OidcCustomScopeAttributeReleasePolicy(final String scopeName, final List<String> allowedAttributes) {
        super(scopeName);
        setAllowedAttributes(allowedAttributes);
    }
}
