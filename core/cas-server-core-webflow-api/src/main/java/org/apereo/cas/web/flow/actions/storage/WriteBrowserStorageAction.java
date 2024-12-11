package org.apereo.cas.web.flow.actions.storage;

import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.DefaultBrowserStorage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;
import lombok.Setter;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WriteBrowserStorageAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Setter
public class WriteBrowserStorageAction extends BaseBrowserStorageAction {
    public WriteBrowserStorageAction(final CasCookieBuilder ticketGrantingCookieBuilder) {
        super(ticketGrantingCookieBuilder);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val ticketGrantingTicket = (String) requestContext.getCurrentEvent().getAttributes().get(TicketGrantingTicket.class.getName());
        val payload = CollectionUtils.wrap(ticketGrantingCookieBuilder.getCookieName(),
            ticketGrantingCookieBuilder.getCasCookieValueManager().buildCookieValue(ticketGrantingTicket, request));
        val sessionStorage = DefaultBrowserStorage.builder()
            .context(browserStorageContextKey)
            .storageType(determineStorageType(requestContext))
            .build()
            .setPayloadJson(payload);
        WebUtils.putBrowserStorage(requestContext, sessionStorage);
        return success(sessionStorage);
    }
}
