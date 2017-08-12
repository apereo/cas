package org.apereo.cas.web.flow;

import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface SingleSignOnParticipationStrategy {

    /**
     * Tries to determine if this request should participate in SSO.
     * Services can opt out of SSO conditionally either per policy
     * or per request parameters. Various internal processes in CAS
     * also try to determine whether SSO should be honored for this request.
     *
     * @param context the request
     * @return true if authn is renewed
     */
    boolean isParticipating(RequestContext context);
}
