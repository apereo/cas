package org.apereo.cas.oidc.claims;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.List;

/**
 * This is {@link OidcAddressScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcAddressScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    private static final long serialVersionUID = 1532960981124784595L;

    private List<String> allowedAttributes = Arrays.asList("address");

    public OidcAddressScopeAttributeReleasePolicy() {
        super("address");
        setAllowedAttributes(allowedAttributes);
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }
}
