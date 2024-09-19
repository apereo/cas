package org.apereo.cas.heimdall.authorizer.resource.policy;

import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import java.io.Serial;
import java.util.HashSet;
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
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RequiredScopesAuthorizationPolicy implements ResourceAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -2444481042826672523L;

    /**
     * Collection of required scopes
     * for this service to proceed.
     */
    private Set<String> scopes = new HashSet<>();

    @Override
    public AuthorizationResult evaluate(final AuthorizableResource resource, final AuthorizationRequest request) {
        val principalAttributes = request.getPrincipal().getAttributes();
        val availableScopes = new HashSet<>(principalAttributes.get(OAuth20Constants.SCOPE));
        return availableScopes.containsAll(scopes)
            ? AuthorizationResult.granted("OK")
            : AuthorizationResult.denied("Denied");
    }
}
