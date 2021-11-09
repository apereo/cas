package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * A web action to check the user (status).
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Slf4j
public class InweboCheckUserAction extends AbstractAction {

    private final InweboService service;

    private final CasConfigurationProperties casProperties;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getInProgressAuthentication();
        val login = authentication.getPrincipal().getId();
        LOGGER.trace("Login: [{}]", login);

        val flowScope = requestContext.getFlowScope();
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        flowScope.put(WebflowConstants.SITE_ALIAS, inwebo.getSiteAlias());
        flowScope.put(WebflowConstants.SITE_DESCRIPTION, inwebo.getSiteDescription());
        flowScope.put(WebflowConstants.LOGIN, login);

        try {
            val response = service.loginSearch(login);
            val oneUser = response.isOk() && response.getCount() == 1 && response.getUserId() > 0;
            if (oneUser) {
                val userIsBlocked = response.getUserStatus() == 1;
                if (userIsBlocked) {
                    LOGGER.error("User is blocked: [{}]", login);
                    return error();
                }
                val activationStatus = response.getActivationStatus();
                if (activationStatus == 0) {
                    LOGGER.debug("User is not registered: [{}]", login);
                    flowScope.put(WebflowConstants.MUST_ENROLL, true);
                    WebUtils.addErrorMessageToContext(requestContext, "cas.inwebo.error.usernotregistered");
                } else if (activationStatus == 1) {
                    LOGGER.debug("User can only handle push notifications: [{}]", login);
                    return getEventFactorySupport().event(this, WebflowConstants.PUSH);
                } else if (activationStatus == 2) {
                    LOGGER.debug("User can only handle browser authentication: [{}]", login);
                    return getEventFactorySupport().event(this, WebflowConstants.BROWSER);
                } else if (activationStatus == 3 || activationStatus == 5) {
                    LOGGER.debug("User must select the authentication method: [{}]", login);
                    return getEventFactorySupport().event(this, WebflowConstants.SELECT);
                } else {
                    LOGGER.error("Unknown activation status: [{}] for: [{}]", activationStatus, login);
                }
            } else {
                LOGGER.error("No user found for: [{}]", login);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, "Cannot search authentication methods", e);
        }
        return error();
    }
}
