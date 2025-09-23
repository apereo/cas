package org.apereo.cas.web.flow.logout;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link FinishLogoutAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class FinishLogoutAction extends AbstractLogoutAction {
    public FinishLogoutAction(final TicketRegistry ticketRegistry,
                              final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                              final ArgumentExtractor argumentExtractor,
                              final ServicesManager servicesManager,
                              final LogoutExecutionPlan logoutExecutionPlan,
                              final CasConfigurationProperties casProperties) {
        super(ticketRegistry, ticketGrantingTicketCookieGenerator,
            argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
    }

    @Override
    protected Event doInternalExecute(final RequestContext context) {
        val logoutRedirect = WebUtils.getLogoutRedirectUrl(context, String.class);
        if (StringUtils.isNotBlank(logoutRedirect)) {
            LOGGER.debug("Redirecting to [{}]", logoutRedirect);
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_REDIRECT);
        }
        val logoutPostUrl = WebUtils.getLogoutPostUrl(context);
        val logoutPostData = WebUtils.getLogoutPostData(context);
        if (StringUtils.isNotBlank(logoutPostUrl) && logoutPostData != null) {
            val flowScope = context.getFlowScope();
            flowScope.put("originalUrl", logoutPostUrl);
            flowScope.put("parameters", logoutPostData);
            LOGGER.debug("Submitting POST logout request to [{}] with parameters [{}]", logoutPostUrl, logoutPostData);
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_POST);
        }
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_FINISH);
    }
}
