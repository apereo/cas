package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;

import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Optional;

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
     * @return result/status if policy is accepted along with principal.
     */
    AcceptableUsagePolicyStatus verify(RequestContext requestContext, Credential credential);

    /**
     * Record the fact that the policy is accepted..
     *
     * @param requestContext the request context
     * @param credential     the credential
     * @return true if choice was saved.
     */
    boolean submit(RequestContext requestContext, Credential credential);

    /**
     * Fetch policy as optional.
     *
     * @param requestContext the request context
     * @param credential     the credential
     * @return the optional
     */
    Optional<AcceptableUsagePolicyTerms> fetchPolicy(RequestContext requestContext, Credential credential);

}
