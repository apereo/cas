package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.OidcConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * This is {@link OidcPhoneScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcPhoneScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    /**
     * Claims allowed by this attribute release policy.
     */
    public static final List<String> ALLOWED_CLAIMS = List.of("phone_number", "phone_number_verified");

    private static final long serialVersionUID = 1532960981124784595L;

    public OidcPhoneScopeAttributeReleasePolicy() {
        super(OidcConstants.StandardScopes.PHONE.getScope());
        setAllowedAttributes(ALLOWED_CLAIMS);
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }
}
