package org.apereo.cas.oidc.claims;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apereo.cas.oidc.OidcConstants;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link OidcProfileScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcProfileScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    private static final long serialVersionUID = 1532960981124784595L;

    private List<String> allowedAttributes = Stream.of("name", "family_name", "given_name",
            "middle_name", "nickname", "preferred_username",
            "profile", "picture", "website",
            "gender", "birthdate", "zoneinfo",
            "locale", "updated_at").collect(Collectors.toList());

    public OidcProfileScopeAttributeReleasePolicy() {
        super(OidcConstants.StandardScopes.PROFILE.getScope());
        setAllowedAttributes(allowedAttributes);
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }
}
