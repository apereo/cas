package org.apereo.cas.web.flow.logout;

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
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

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
    protected Event doExecuteInternal(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        preventCaching(response);

        Optional.ofNullable(argumentExtractor.extractService(request))
            .filter(service -> {
                val registeredService = servicesManager.findServiceBy(service);
                return registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service);
            })
            .ifPresent(service -> WebUtils.putServiceIntoFlowScope(context, service));

        return doInternalExecute(context);
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
