package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.OidcConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

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
    public static final Map<String,List<String>> ALLOWED_CLAIMS_AND_FIELDS =
            Map.of("address",
                    List.of("formatted", "street_address", "locality", "region", "postal_code", "country"));

    private static final long serialVersionUID = 1532960981124784595L;

    public OidcAddressScopeAttributeReleasePolicy() {
        super(OidcConstants.StandardScopes.ADDRESS.getScope());
        setAllowedNormalClaims(Collections.emptyList());
        setAllowedAggregatedClaims(ALLOWED_CLAIMS_AND_FIELDS);
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedNormalClaims() {
        return super.getAllowedNormalClaims();
    }
}
