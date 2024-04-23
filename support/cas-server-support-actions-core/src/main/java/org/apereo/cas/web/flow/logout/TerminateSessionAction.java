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
import org.springframework.context.ConfigurableApplicationContext;
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

    /**
     * Parameter to indicate logout request is confirmed.
     */
    public static final String REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED = "LogoutRequestConfirmed";

    /**
     * The event factory.
     */
    protected final EventFactorySupport eventFactorySupport = new EventFactorySupport();

    /**
     * The authentication service.
     */
    protected final CentralAuthenticationService centralAuthenticationService;

    /**
     * The TGT cookie generator.
     */
    protected final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    /**
     * The warn cookie generator.
     */
    protected final CasCookieBuilder warnCookieGenerator;

    /**
     * The logout properties.
     */
    protected final LogoutProperties logoutProperties;

    /**
     * Logout manager.
     */
    protected final LogoutManager logoutManager;

    /**
     * Application context.
     */
    protected final ConfigurableApplicationContext applicationContext;

    /**
     * Single logout executor.
     */
    protected final SingleLogoutRequestExecutor singleLogoutRequestExecutor;

    protected static boolean isLogoutRequestConfirmed(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        return request.getParameterMap().containsKey(REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val terminateSession = FunctionUtils.doIf(logoutProperties.isConfirmLogout(),
                () -> isLogoutRequestConfirmed(requestContext),
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

    protected Event terminate(final RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

        val beanFactory = applicationContext.getBeanFactory();
        val terminationHandlers = BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, SessionTerminationHandler.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .toList();

        val tgtId = getTicketGrantingTicket(requestContext);

        if (StringUtils.isNotBlank(tgtId)) {
            LOGGER.trace("Destroying SSO session linked to ticket-granting ticket [{}]", tgtId);
            terminationHandlers.forEach(processor -> processor.beforeSingleLogout(tgtId, requestContext));

            val logoutRequests = initiateSingleLogout(tgtId, request, response);
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
