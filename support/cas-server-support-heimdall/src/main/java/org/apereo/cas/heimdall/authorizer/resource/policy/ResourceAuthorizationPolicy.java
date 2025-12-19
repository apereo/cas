package org.apereo.cas.heimdall.authorizer.resource.policy;

import module java.base;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link ResourceAuthorizationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface ResourceAuthorizationPolicy extends Serializable {
    /**
     * Evaluate authorization result.
     *
     * @param resource the resource
     * @param request  the request
     * @return the authorization result
     * @throws Throwable the exception
     */
    AuthorizationResult evaluate(AuthorizableResource resource, AuthorizationRequest request) throws Throwable;
}
