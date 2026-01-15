package org.apereo.cas.oidc.claims;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;

/**
 * This is {@link OidcEmailScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcEmailScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    /**
     * Claims allowed by this attribute release policy.
     */
    public static final List<String> ALLOWED_CLAIMS = List.of("email", "email_verified");

    @Serial
    private static final long serialVersionUID = 1532960981124784595L;

    public OidcEmailScopeAttributeReleasePolicy() {
        super(OidcConstants.StandardScopes.EMAIL.getScope());
        setAllowedAttributes(ALLOWED_CLAIMS);
    }
}
