package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AcceptableUsagePolicyAccountProfileAction}.
 * Shows in the account profile whether the user has accepted the acceptable usage policy, and the policy terms.
 * The policy repository does not record when the policy was accepted, so no date is shown.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class AcceptableUsagePolicyAccountProfileAction extends BaseCasWebflowAction {
    /**
     * Flow scope attribute for the policy status: {@code accepted}, {@code denied} or {@code skipped}.
     */
    public static final String FLOW_SCOPE_ATTR_STATUS = "acceptableUsagePolicyStatus";

    /**
     * Flow scope attribute for the policy terms.
     */
    public static final String FLOW_SCOPE_ATTR_TERMS = "acceptableUsagePolicyTerms";

    private final AcceptableUsagePolicyRepository repository;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        try {
            val status = repository.verify(requestContext).getStatus();
            val outcome = status.isTrue() ? "accepted" : status.isFalse() ? "denied" : "skipped";
            requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_STATUS, outcome);
            repository.fetchPolicy(requestContext)
                .ifPresent(terms -> requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_TERMS, terms));
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return null;
    }
}
