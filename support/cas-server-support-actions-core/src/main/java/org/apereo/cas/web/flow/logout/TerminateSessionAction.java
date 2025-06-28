package org.apereo.cas.web.flow.logout;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.SessionTerminationHandler;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Terminates the CAS SSO session by destroying all SSO state data (i.e. TGT, cookies).
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TerminateSessionAction extends BaseCasWebflowAction {
    protected final EventFactorySupport eventFactorySupport = eventFactory;

    protected final CentralAuthenticationService centralAuthenticationService;

    protected final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    protected final CasCookieBuilder warnCookieGenerator;

    protected final LogoutProperties logoutProperties;

    protected final LogoutManager logoutManager;

    protected final SingleLogoutRequestExecutor singleLogoutRequestExecutor;
    
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val terminateSession = FunctionUtils.doIf(logoutProperties.isConfirmLogout(),
                () -> WebUtils.isLogoutRequestConfirmed(requestContext),
                () -> Boolean.TRUE)
            .get();

        if (terminateSession) {
            return terminate(requestContext);
        }
        return eventFactorySupport.event(this, CasWebflowConstants.STATE_ID_WARN);
    }

    protected String getTicketGrantingTicket(final RequestContext context) {
        val tgtId = WebUtils.getTicketGrantingTicketId(context);
        if (StringUtils.isBlank(tgtId)) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            return ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }
        return tgtId;
    }

    protected Event terminate(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val terminationHandlers = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, SessionTerminationHandler.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .toList();

        val ticketGrantingTicketId = getTicketGrantingTicket(requestContext);
        if (StringUtils.isNotBlank(ticketGrantingTicketId)) {
            LOGGER.trace("Destroying SSO session linked to ticket-granting ticket [{}]", ticketGrantingTicketId);
            terminationHandlers.forEach(processor -> processor.beforeSingleLogout(ticketGrantingTicketId, requestContext));

            val logoutRequests = initiateSingleLogout(ticketGrantingTicketId, request, response);
            WebUtils.putLogoutRequests(requestContext, logoutRequests);
        }
        LOGGER.trace("Removing CAS cookies");
        ticketGrantingTicketCookieGenerator.removeCookie(response);
        warnCookieGenerator.removeCookie(response);
        destroyApplicationContext(terminationHandlers, requestContext);
        LOGGER.debug("Terminated all CAS sessions successfully.");

        if (StringUtils.isNotBlank(logoutProperties.getRedirectUrl())) {
            WebUtils.putLogoutRedirectUrl(requestContext, logoutProperties.getRedirectUrl());
            return eventFactorySupport.event(this, CasWebflowConstants.STATE_ID_REDIRECT);
        }

        return eventFactorySupport.success(this);
    }

    protected void destroyApplicationContext(final List<SessionTerminationHandler> terminationHandlers,
                                             final RequestContext requestContext) {
        val terminationResults = terminationHandlers
            .stream()
            .map(processor -> processor.beforeSessionTermination(requestContext))
            .flatMap(List::stream)
            .toList();
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val session = request.getSession(false);
        FunctionUtils.doIfNotNull(session, HttpSession::invalidate);
        terminationHandlers.forEach(processor -> processor.afterSessionTermination(terminationResults, requestContext));
    }

    protected List<SingleLogoutRequestContext> initiateSingleLogout(final String ticketGrantingTicketId,
                                                                    final HttpServletRequest request,
                                                                    final HttpServletResponse response) {
        return singleLogoutRequestExecutor.execute(ticketGrantingTicketId, request, response);
    }
}
