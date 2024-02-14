package org.apereo.cas.oidc.web.flow.account;

import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OidcAccountProfileRemoveAccessTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcAccountProfileRemoveAccessTokenAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val tokenId = requestContext.getRequestParameters().getRequired("id");
        ticketRegistry.deleteTicket(tokenId);
        return success();
    }
}
