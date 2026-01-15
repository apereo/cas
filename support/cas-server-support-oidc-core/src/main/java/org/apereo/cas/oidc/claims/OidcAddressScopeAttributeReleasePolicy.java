package org.apereo.cas.oidc.claims;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;

/**
 * This is {@link OidcAddressScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcAddressScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    /**
     * Claims allowed by this attribute release policy.
     */
    public static final List<String> ALLOWED_CLAIMS = List.of("address");

    @Serial
    private static final long serialVersionUID = 1532960981124784595L;

    public OidcAddressScopeAttributeReleasePolicy() {
        super(OidcConstants.StandardScopes.ADDRESS.getScope());
        setAllowedAttributes(ALLOWED_CLAIMS);
    }
}
