package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.OidcConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * This is {@link OidcProfileScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcProfileScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    /**
     * Claims allowed by this attribute release policy.
     */
    public static final List<String> ALLOWED_CLAIMS = List.of("name", "family_name", "given_name",
        "middle_name", "nickname", "preferred_username", "profile", "picture", "website",
        "gender", "birthdate", "zoneinfo", "locale", "updated_at");

    private static final long serialVersionUID = 1532960981124784595L;

    public OidcProfileScopeAttributeReleasePolicy() {
        super(OidcConstants.StandardScopes.PROFILE.getScope());
        setAllowedAttributes(ALLOWED_CLAIMS);
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }
}
