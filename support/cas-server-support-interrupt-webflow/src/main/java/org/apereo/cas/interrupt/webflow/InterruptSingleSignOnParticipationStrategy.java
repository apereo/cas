package org.apereo.cas.interrupt.webflow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategy;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InterruptSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class InterruptSingleSignOnParticipationStrategy extends DefaultSingleSignOnParticipationStrategy {
    public InterruptSingleSignOnParticipationStrategy(final ServicesManager servicesManager, final boolean createSsoOnRenewedAuthn,
                                                      final boolean renewEnabled) {
        super(servicesManager, createSsoOnRenewedAuthn, renewEnabled);
    }

    @Override
    public boolean isParticipating(final RequestContext ctx) {
        final var response = InterruptUtils.getInterruptFrom(ctx);
        if (response != null && !response.isSsoEnabled()) {
            return false;
        }
        return super.isParticipating(ctx);
    }
}
