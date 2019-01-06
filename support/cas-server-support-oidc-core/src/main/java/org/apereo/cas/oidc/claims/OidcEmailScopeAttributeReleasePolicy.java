package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.OidcConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link OidcEmailScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcEmailScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    private static final long serialVersionUID = 1532960981124784595L;

    private static final List<String> ALLOWED_ATTRIBUTES = Stream.of("email", "email_verified").collect(Collectors.toList());

    public OidcEmailScopeAttributeReleasePolicy() {
        super(OidcConstants.StandardScopes.EMAIL.getScope());
        setAllowedAttributes(ALLOWED_ATTRIBUTES);
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }
}
