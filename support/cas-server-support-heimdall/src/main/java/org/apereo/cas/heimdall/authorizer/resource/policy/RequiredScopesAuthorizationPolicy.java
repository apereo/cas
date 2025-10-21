package org.apereo.cas.heimdall.authorizer.resource.policy;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.util.Set;

/**
 * This is {@link RequiredScopesAuthorizationPolicy}.
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
public class RequiredScopesAuthorizationPolicy extends RequiredAttributesAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -2444481042826672523L;

    @JsonCreator
    public RequiredScopesAuthorizationPolicy(@JsonProperty("scopes") final Set<String> scopes) {
        setAttributes(CollectionUtils.wrap(OAuth20Constants.SCOPE, scopes));
    }
}
