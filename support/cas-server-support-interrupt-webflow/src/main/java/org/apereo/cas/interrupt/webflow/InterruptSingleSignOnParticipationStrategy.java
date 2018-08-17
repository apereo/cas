package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategy;

import lombok.val;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InterruptSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InterruptSingleSignOnParticipationStrategy extends DefaultSingleSignOnParticipationStrategy {
    public InterruptSingleSignOnParticipationStrategy(final ServicesManager servicesManager, final boolean createSsoOnRenewedAuthn,
                                                      final boolean renewEnabled) {
        super(servicesManager, createSsoOnRenewedAuthn, renewEnabled);
    }

    @Override
    public boolean isParticipating(final RequestContext ctx) {
        val response = InterruptUtils.getInterruptFrom(ctx);
        if (response != null && !response.isSsoEnabled()) {
            return false;
        }
        return super.isParticipating(ctx);
    }
}
