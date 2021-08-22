package org.apereo.cas.web.flow.logout;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link FinishLogoutAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class FinishLogoutAction extends AbstractLogoutAction {
    public FinishLogoutAction(final CentralAuthenticationService centralAuthenticationService,
                              final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                              final ArgumentExtractor argumentExtractor,
                              final ServicesManager servicesManager,
                              final LogoutExecutionPlan logoutExecutionPlan,
                              final CasConfigurationProperties casProperties) {
        super(centralAuthenticationService, ticketGrantingTicketCookieGenerator,
            argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
    }

    @Override
    protected Event doInternalExecute(final HttpServletRequest request,
                                      final HttpServletResponse response,
                                      final RequestContext context) {
        val logoutRedirect = WebUtils.getLogoutRedirectUrl(context, String.class);
        if (StringUtils.isNotBlank(logoutRedirect)) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_REDIRECT);
        }
        val logoutPostUrl = WebUtils.getLogoutPostUrl(context);
        val logoutPostData = WebUtils.getLogoutPostData(context);
        if (StringUtils.isNotBlank(logoutPostUrl) && logoutPostData != null) {
            val flowScope = context.getFlowScope();
            flowScope.put("originalUrl", logoutPostUrl);
            flowScope.put("parameters", logoutPostData);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_POST);
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_FINISH);
    }
}
