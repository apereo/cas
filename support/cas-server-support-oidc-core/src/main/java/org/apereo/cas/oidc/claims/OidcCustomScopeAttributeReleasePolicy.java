package org.apereo.cas.oidc.claims;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OidcCustomScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcCustomScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    /**
     * Custom misc scope.
     */
    public static final String SCOPE_CUSTOM = "custom";

    private static final long serialVersionUID = -8338967628001071540L;

    public OidcCustomScopeAttributeReleasePolicy() {
        this(new ArrayList<>());
    }

    public OidcCustomScopeAttributeReleasePolicy(final List<String> allowedAttributes) {
        this(SCOPE_CUSTOM, allowedAttributes);
    }

    public OidcCustomScopeAttributeReleasePolicy(final String scopeName, final List<String> allowedAttributes) {
        super(scopeName);
        setAllowedAttributes(allowedAttributes);
    }
}
