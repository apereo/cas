package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
    protected Event doExecuteInternal(final RequestContext requestContext) {
        var eventId = requestContext.getRequestParameters().get("mfaProvider", String.class);
        if (StringUtils.isBlank(eventId)) {
            val provider = requestContext.getFlashScope().get("mfaProvider", MultifactorAuthenticationProvider.class);
            eventId = provider.getId();
        }
        return new EventFactorySupport().event(this, eventId);
    }
}
