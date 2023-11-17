package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.OidcConstants;
import java.io.Serial;
import java.util.List;

/**
 * This is {@link OidcAssuranceScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OidcAssuranceScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    /**
     * Claims allowed by this attribute release policy.
     */
    public static final List<String> ALLOWED_CLAIMS = List.of("place_of_birth", "nationalities",
        "birth_family_name", "birth_given_name", "birth_middle_name", "salutation", "title", "msisdn",
        "also_known_as");

    @Serial
    private static final long serialVersionUID = 5523450982224784565L;

    public OidcAssuranceScopeAttributeReleasePolicy() {
        super(OidcConstants.StandardScopes.ASSURANCE.getScope());
        setAllowedAttributes(ALLOWED_CLAIMS);
    }
}
