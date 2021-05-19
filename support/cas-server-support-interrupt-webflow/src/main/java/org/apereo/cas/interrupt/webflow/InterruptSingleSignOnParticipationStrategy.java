package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import java.util.Objects;

/**
 * This is {@link InterruptSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InterruptSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {

    @Override
    public boolean supports(final SingleSignOnParticipationRequest context) {
        return context.getRequestContext().stream()
            .map(InterruptUtils::getInterruptFrom)
            .anyMatch(Objects::nonNull);
    }

    @Override
    public boolean isParticipating(final SingleSignOnParticipationRequest context) {
        return context.getRequestContext().stream()
            .map(InterruptUtils::getInterruptFrom)
            .filter(Objects::nonNull)
            .allMatch(InterruptResponse::isSsoEnabled);
    }

    @Override
    public TriStateBoolean isCreateCookieOnRenewedAuthentication(final SingleSignOnParticipationRequest context) {
        return TriStateBoolean.FALSE;
    }
}
