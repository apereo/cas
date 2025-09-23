package org.apereo.cas.heimdall.authorizer.resource.policy;

import org.apereo.cas.oidc.OidcConstants;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RequiredACRAuthorizationPolicy}.
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
public class RequiredACRAuthorizationPolicy extends RequiredAttributesAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -2433481042826672523L;

    @JsonCreator
    public RequiredACRAuthorizationPolicy(@JsonProperty("acrs") final Set<String> acrs) {
        setAttributes(Map.of(OidcConstants.ACR, acrs));
    }
}
