package org.apereo.cas.web.flow.actions;

import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.DefaultBrowserStorage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Setter
public class WriteBrowserStorageAction extends BaseCasWebflowAction {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(true).build().toObjectMapper();

    private String browserStorageContextKey = "casBrowserStorageContext";

    private final CasCookieBuilder ticketGrantingCookieBuilder;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val ticketGrantingTicket = (String) requestContext.getCurrentEvent().getAttributes().get(TicketGrantingTicket.class.getName());
        val payload = CollectionUtils.wrap(ticketGrantingCookieBuilder.getCookieName(),
            ticketGrantingCookieBuilder.getCasCookieValueManager().buildCookieValue(ticketGrantingTicket, request));
        val sessionStorage = DefaultBrowserStorage.builder()
            .payload(MAPPER.writeValueAsString(payload))
            .context(browserStorageContextKey)
            .build();
        requestContext.getFlowScope().put(BrowserStorage.PARAMETER_BROWSER_STORAGE, sessionStorage);
        return success(sessionStorage);
    }
}
