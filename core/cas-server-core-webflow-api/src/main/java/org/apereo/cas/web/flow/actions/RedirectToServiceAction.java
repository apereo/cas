package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Locale;

/**
 * This is {@link RedirectToServiceAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedirectToServiceAction extends BaseCasWebflowAction {
    private final ResponseBuilderLocator<WebApplicationService> responseBuilderLocator;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
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

    protected Event finalizeResponseEvent(final RequestContext requestContext, final WebApplicationService service, final Response response) {
        WebUtils.putServiceResponseIntoRequestScope(requestContext, response);
        WebUtils.putServiceOriginalUrlIntoRequestScope(requestContext, service);
        val eventId = getFinalResponseEventId(service, response, requestContext);
        return eventFactory.event(this, eventId);
    }

    protected String getFinalResponseEventId(final WebApplicationService service, final Response response, final RequestContext requestContext) {
        val eventId = response.responseType().name().toLowerCase(Locale.ENGLISH);
        LOGGER.debug("Signaling flow to redirect to service [{}] via event [{}]", service, eventId);
        return eventId;
    }
}
