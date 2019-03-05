package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RedirectToServiceAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedirectToServiceAction extends AbstractAction {
    private final ResponseBuilderLocator<WebApplicationService> responseBuilderLocator;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val service = WebUtils.getService(requestContext);
        LOGGER.debug("Located service [{}] from the context", service);

        val auth = WebUtils.getAuthentication(requestContext);
        LOGGER.debug("Located authentication [{}] from the context", auth);

        val serviceTicketId = WebUtils.getServiceTicketFromRequestScope(requestContext);
        LOGGER.debug("Located service ticket [{}] from the context", serviceTicketId);

        val builder = responseBuilderLocator.locate(service);
        LOGGER.debug("Located service response builder [{}] for [{}]", builder, service);

        val response = builder.build(service, serviceTicketId, auth);
        LOGGER.debug("Built response [{}] for [{}]", response, service);

        return finalizeResponseEvent(requestContext, service, response);
    }

    /**
     * Finalize response event event.
     *
     * @param requestContext the request context
     * @param service        the service
     * @param response       the response
     * @return the event
     */
    protected Event finalizeResponseEvent(final RequestContext requestContext, final WebApplicationService service, final Response response) {
        WebUtils.putServiceResponseIntoRequestScope(requestContext, response);
        WebUtils.putServiceOriginalUrlIntoRequestScope(requestContext, service);
        val eventId = getFinalResponseEventId(service, response, requestContext);
        return new EventFactorySupport().event(this, eventId);
    }

    /**
     * Gets final response event id.
     *
     * @param service        the service
     * @param response       the response
     * @param requestContext the request context
     * @return the final response event id
     */
    protected String getFinalResponseEventId(final WebApplicationService service, final Response response, final RequestContext requestContext) {
        val eventId = response.getResponseType().name().toLowerCase();
        LOGGER.debug("Signaling flow to redirect to service [{}] via event [{}]", service, eventId);
        return eventId;
    }
}
