package org.apereo.cas.aup;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;

/**
 * This is {@link AcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface AcceptableUsagePolicyRepository extends Serializable {
    /**
     * Verify whether the policy is accepted.
     *
     * @param requestContext the request context
     * @param credential     the credential
     * @return pair true if policy is accepted along with principal.
     */
    Pair<Boolean, Principal> verify(RequestContext requestContext, Credential credential);

    /**
     * Record the fact that the policy is accepted..
     *
     * @param requestContext the request context
     * @param credential     the credential
     * @return true if choice was saved.
     */
    boolean submit(RequestContext requestContext, Credential credential);

}
