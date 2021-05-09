package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.OidcConstants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This is {@link OidcCustomScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class OidcCustomScopeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    private static final long serialVersionUID = -8338967628001071540L;

    private String scopeName;

    @JsonCreator
    public OidcCustomScopeAttributeReleasePolicy(@JsonProperty("scopeName") final String scopeName,
                                                 @JsonProperty("allowedAttributes") final List<String> allowedAttributes) {
        super(OidcConstants.CUSTOM_SCOPE_TYPE);
        this.scopeName = scopeName;
        setAllowedAttributes(allowedAttributes);
    }
}
