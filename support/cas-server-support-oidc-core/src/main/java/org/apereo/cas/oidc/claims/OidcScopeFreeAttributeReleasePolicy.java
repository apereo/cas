package org.apereo.cas.oidc.claims;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.util.List;

/**
 * This is {@link OidcScopeFreeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class OidcScopeFreeAttributeReleasePolicy extends BaseOidcScopeAttributeReleasePolicy {
    /**
     * Indicates that the scope is not defined and any attribute release policy.
     */
    static final String ANY_SCOPE = "*";
    
    @Serial
    private static final long serialVersionUID = -8338967628001071540L;

    @JsonCreator
    public OidcScopeFreeAttributeReleasePolicy() {
        super(ANY_SCOPE);
    }
    
    @JsonCreator
    public OidcScopeFreeAttributeReleasePolicy(@JsonProperty("allowedAttributes") final List<String> allowedAttributes) {
        this();
        setAllowedAttributes(allowedAttributes);
    }

    @Override
    public boolean claimsMustBeDefinedViaDiscovery() {
        return false;
    }
}
