package org.apereo.cas.heimdall.authorizer.resource.policy;

import module java.base;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;

/**
 * This is {@link RejectedAttributesAuthorizationPolicy}.
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
public class RejectedAttributesAuthorizationPolicy extends RequiredAttributesAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -2433481042826672523L;

    @Override
    public AuthorizationResult evaluate(final AuthorizableResource resource, final AuthorizationRequest request) {
        val result = super.evaluate(resource, request);
        return result.authorized() ? AuthorizationResult.denied("Denied") : AuthorizationResult.granted("OK");
    }
}
