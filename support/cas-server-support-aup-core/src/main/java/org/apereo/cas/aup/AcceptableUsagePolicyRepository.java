package org.apereo.cas.aup;

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
     * @return result/status if policy is accepted along with principal.
     */
    AcceptableUsagePolicyStatus verify(RequestContext requestContext);

    /**
     * Record the fact that the policy is accepted..
     *
     * @param requestContext the request context
     * @return true if choice was saved.
     */
    boolean submit(RequestContext requestContext);

    /**
     * Fetch policy as optional.
     *
     * @param requestContext the request context
     * @return the optional
     */
    Optional<AcceptableUsagePolicyTerms> fetchPolicy(RequestContext requestContext);

}
