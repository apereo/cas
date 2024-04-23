package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.OidcConstants;
import java.io.Serial;
import java.util.ArrayList;

/**
 * This is {@link OidcOfflineAccessScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcOfflineAccessScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    @Serial
    private static final long serialVersionUID = 1532960981124784595L;

    public OidcOfflineAccessScopeAttributeReleasePolicy() {
        super(OidcConstants.StandardScopes.OFFLINE_ACCESS.getScope());
        setAllowedAttributes(new ArrayList<>());
    }
}
