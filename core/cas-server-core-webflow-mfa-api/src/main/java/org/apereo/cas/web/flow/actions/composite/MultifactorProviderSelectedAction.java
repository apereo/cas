package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;

import lombok.val;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorProviderSelectedAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class MultifactorProviderSelectedAction extends BaseCasWebflowAction {
    @Override
    public Event doExecute(final RequestContext requestContext) {
        val eventId = requestContext.getRequestParameters().get("mfaProvider", String.class);
        return new EventFactorySupport().event(this, eventId);
    }
}
