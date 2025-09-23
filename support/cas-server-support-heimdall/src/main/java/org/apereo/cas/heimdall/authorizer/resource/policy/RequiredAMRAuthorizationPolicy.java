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
 * This is {@link RequiredAMRAuthorizationPolicy}.
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
public class RequiredAMRAuthorizationPolicy extends RequiredAttributesAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -2433481042826672523L;

    @JsonCreator
    public RequiredAMRAuthorizationPolicy(@JsonProperty("amrs") final Set<String> amrs) {
        setAttributes(Map.of(OidcConstants.AMR, amrs));
    }
}
