package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.OidcConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OidcOpenIdScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcOpenIdScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    private static final long serialVersionUID = 1532960981124784595L;

    public OidcOpenIdScopeAttributeReleasePolicy() {
        super(OidcConstants.StandardScopes.OPENID.getScope());
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return new ArrayList<>(0);
    }
}
