package org.apereo.cas.web.flow.account;

import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccountProfileRemoveSingleSignOnSessionAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class AccountProfileRemoveSingleSignOnSessionAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val tgt = WebUtils.getTicketGrantingTicketId(requestContext);
        val id = requestContext.getRequestParameters().get("id", String.class);
        ticketRegistry.deleteTicket(id);
        return tgt.equals(id) ? new Event(this, CasWebflowConstants.TRANSITION_ID_VALIDATE) : success();
    }
}
