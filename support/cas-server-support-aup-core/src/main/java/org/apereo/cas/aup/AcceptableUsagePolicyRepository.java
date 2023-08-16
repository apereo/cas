package org.apereo.cas.aup;

import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;
import java.io.Serial;
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
     * Condition to activate AUP.
     */
    BeanCondition CONDITION_AUP_ENABLED = BeanCondition.on("cas.acceptable-usage-policy.core.enabled").isTrue().evenIfMissing();

    /**
     * Default bean name.
     */
    String BEAN_NAME = "acceptableUsagePolicyRepository";

    /**
     * No op acceptable usage policy repository.
     *
     * @return the acceptable usage policy repository
     */
    static AcceptableUsagePolicyRepository noOp() {
        return new AcceptableUsagePolicyRepository() {
            @Serial
            private static final long serialVersionUID = 8784500942988440997L;

            @Override
            public AcceptableUsagePolicyStatus verify(final RequestContext requestContext) {
                val authn = WebUtils.getAuthentication(requestContext);
                return AcceptableUsagePolicyStatus.skipped(authn.getPrincipal());
            }

            @Override
            public boolean submit(final RequestContext requestContext) {
                return false;
            }

            @Override
            public Optional<AcceptableUsagePolicyTerms> fetchPolicy(final RequestContext requestContext) {
                return Optional.empty();
            }
        };
    }

    /**
     * Verify whether the policy is accepted.
     *
     * @param requestContext the request context
     * @return result /status if policy is accepted along with principal.
     * @throws Throwable the throwable
     */
    AcceptableUsagePolicyStatus verify(RequestContext requestContext) throws Throwable;

    /**
     * Record the fact that the policy is accepted..
     *
     * @param requestContext the request context
     * @return true if choice was saved.
     * @throws Throwable the throwable
     */
    boolean submit(RequestContext requestContext) throws Throwable;

    /**
     * Fetch policy as optional.
     *
     * @param requestContext the request context
     * @return the optional
     */
    Optional<AcceptableUsagePolicyTerms> fetchPolicy(RequestContext requestContext);
}
