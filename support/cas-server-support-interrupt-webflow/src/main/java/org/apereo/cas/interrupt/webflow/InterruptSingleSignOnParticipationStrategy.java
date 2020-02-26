package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import lombok.val;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InterruptSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InterruptSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {

    @Override
    public boolean supports(final RequestContext context) {
        val response = InterruptUtils.getInterruptFrom(context);
        return response != null;
    }

    @Override
    public boolean isParticipating(final RequestContext ctx) {
        val response = InterruptUtils.getInterruptFrom(ctx);
        return response != null && response.isSsoEnabled();
    }

    @Override
    public TriStateBoolean isCreateCookieOnRenewedAuthentication(final RequestContext context) {
        return TriStateBoolean.FALSE;
    }
}
