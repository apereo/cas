package org.apereo.cas.oidc.claims.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link DefaultOidcAttributeToScopeClaimMapper}.
 * In order for attributes to be released when using a scope in OIDC, attributes need to mapped
 * in to the correct OIDC name (i.e. {@code given_name}) for those attributes.
 * The side effect is that the attribute will always be released as the mapped name
 * even for non-OIDC requests.
 * This component allows for an arbitrary mapping between the predefined OIDC claims and attributes.
 * If a mapping is found for a claim, then the attribute mapped to the claim will be used.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultOidcAttributeToScopeClaimMapper implements OidcAttributeToScopeClaimMapper {

    /**
     * Map OIDC claim to an attribute.
     */
    private Map<String, String> claimsToAttribute = new HashMap<>();

    public DefaultOidcAttributeToScopeClaimMapper(final Map<String, String> claimsToAttribute) {
        this.claimsToAttribute = claimsToAttribute;
    }

    @Override
    public String getMappedAttribute(final String claim) {
        return claimsToAttribute.get(claim);
    }

    @Override
    public boolean containsMappedAttribute(final String claim) {
        return claimsToAttribute.containsKey(claim);
    }
}
