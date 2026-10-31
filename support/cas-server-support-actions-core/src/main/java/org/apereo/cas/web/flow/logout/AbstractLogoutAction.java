package org.apereo.cas.web.flow.logout;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Abstract logout action, which prevents caching on logout.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
@RequiredArgsConstructor
public abstract class AbstractLogoutAction extends BaseCasWebflowAction {

    private static final String NO_CACHE = "no-cache";

    private static final String CACHE_CONTROL = "Cache-Control";

    private static final String REQUEST_SCOPE_ATTRIBUTE_SERVICE_RESOLVED = AbstractLogoutAction.class.getName() + ".serviceResolved";

    /**
     * The cas service.
     */
    protected final TicketRegistry ticketRegistry;

    /**
     * The TGT cookie generator.
     */
    protected final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    /**
     * Argument extractor.
     */
    protected final ArgumentExtractor argumentExtractor;

    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Logout execution plan.
     */
    protected final LogoutExecutionPlan logoutExecutionPlan;

    /**
     * CAS properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        preventCaching(response);
        resolveServiceOncePerRequest(context, request);
        return doInternalExecute(context);
    }

    /**
     * Put the requested service into the flow scope when it is registered and allowed. Every logout action runs this,
     * and several of them run in the same request, so the outcome, found or not, is remembered in the request scope
     * and the service is looked up only once per request.
     *
     * @param context the request context
     * @param request the http request
     */
    protected void resolveServiceOncePerRequest(final RequestContext context, final HttpServletRequest request) {
        val requestScope = context.getRequestScope();
        if (!requestScope.contains(REQUEST_SCOPE_ATTRIBUTE_SERVICE_RESOLVED)) {
            requestScope.put(REQUEST_SCOPE_ATTRIBUTE_SERVICE_RESOLVED, Boolean.TRUE);
            Optional.ofNullable(argumentExtractor.extractService(request))
                .filter(service -> {
                    val registeredService = servicesManager.findServiceBy(service);
                    return registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service);
                })
                .ifPresent(service -> WebUtils.putServiceIntoFlowScope(context, service));
        }
    }

    protected abstract Event doInternalExecute(RequestContext context);

    /**
     * Prevent caching by adding the appropriate headers.
     * Copied from the {@code preventCaching} method in the
     * {@link org.springframework.web.servlet.support.WebContentGenerator} class.
     *
     * @param response the HTTP response.
     */
    protected void preventCaching(final HttpServletResponse response) {
        response.setHeader("Pragma", NO_CACHE);
        response.setDateHeader("Expires", 1L);
        response.setHeader(CACHE_CONTROL, NO_CACHE);
        response.addHeader(CACHE_CONTROL, "no-store");
    }
}
