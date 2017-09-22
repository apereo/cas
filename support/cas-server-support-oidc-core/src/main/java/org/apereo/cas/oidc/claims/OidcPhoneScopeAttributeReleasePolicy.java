package org.apereo.cas.oidc.claims;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apereo.cas.oidc.OidcConstants;

import java.util.Arrays;
import java.util.List;

/**
 * This is {@link OidcPhoneScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcPhoneScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    private static final long serialVersionUID = 1532960981124784595L;

    private final List<String> allowedAttributes = Arrays.asList("phone_number", "phone_number_verified");

    public OidcPhoneScopeAttributeReleasePolicy() {
        super(OidcConstants.PHONE);
        setAllowedAttributes(allowedAttributes);
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }
}
