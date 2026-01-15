package org.apereo.cas.heimdall.authorizer.resource.policy;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This is {@link RequiredAudienceAuthorizationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
public class RequiredAudienceAuthorizationPolicy extends RequiredAttributesAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -2444481042826672523L;

    @JsonCreator
    public RequiredAudienceAuthorizationPolicy(@JsonProperty("audience") final Set<String> audience) {
        setAttributes(Map.of(OidcConstants.AUD, audience));
    }
}
