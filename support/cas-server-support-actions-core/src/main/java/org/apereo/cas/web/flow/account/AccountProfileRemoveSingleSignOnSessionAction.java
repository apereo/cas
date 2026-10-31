package org.apereo.cas.web.flow.account;

import module java.base;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccountProfileRemoveSingleSignOnSessionAction}.
 * Only sessions that belong to the principal of the current single sign-on session can be removed.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class AccountProfileRemoveSingleSignOnSessionAction extends BaseCasWebflowAction {
    private final SingleLogoutRequestExecutor singleLogoutRequestExecutor;

    private final TicketRegistry ticketRegistry;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val tgt = WebUtils.getTicketGrantingTicketId(requestContext);
        val id = requestContext.getRequestParameters().getRequired("id");
        if (!isOwnedByCurrentPrincipal(tgt, id)) {
            LOGGER.warn("Single sign-on session [{}] does not belong to the authenticated principal and cannot be removed", id);
            return error();
        }
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        singleLogoutRequestExecutor.execute(id, request, response);
        return id.equals(tgt) ? new Event(this, CasWebflowConstants.TRANSITION_ID_VALIDATE) : success();
    }

    /**
     * Whether the session to remove is the current session or another session of the same principal.
     *
     * @param currentTicketGrantingTicketId the current ticket granting ticket id
     * @param id                            the id of the session to remove
     * @return true if the session can be removed
     */
    protected boolean isOwnedByCurrentPrincipal(final @Nullable String currentTicketGrantingTicketId, final String id) {
        if (currentTicketGrantingTicketId == null) {
            return false;
        }
        if (currentTicketGrantingTicketId.equals(id)) {
            return true;
        }
        return ticketRegistry.getTicket(currentTicketGrantingTicketId) instanceof final TicketGrantingTicket current
            && ticketRegistry.getTicket(id) instanceof final TicketGrantingTicket target
            && current.getAuthentication().getPrincipal().getId().equals(target.getAuthentication().getPrincipal().getId());
    }
}
