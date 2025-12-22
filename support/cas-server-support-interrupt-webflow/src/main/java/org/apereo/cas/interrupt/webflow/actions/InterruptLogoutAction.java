package org.apereo.cas.interrupt.webflow.actions;

import module java.base;
import org.apereo.cas.interrupt.InterruptTrackingEngine;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InterruptLogoutAction}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
public class InterruptLogoutAction extends BaseCasWebflowAction {
    private final InterruptTrackingEngine interruptTrackingEngine;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        interruptTrackingEngine.removeInterrupt(requestContext);
        return null;
    }
}
