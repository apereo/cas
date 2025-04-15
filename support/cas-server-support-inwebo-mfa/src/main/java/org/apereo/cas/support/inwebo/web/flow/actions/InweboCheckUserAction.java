package org.apereo.cas.support.inwebo.web.flow.actions;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static org.apereo.cas.configuration.model.support.mfa.InweboMultifactorAuthenticationProperties.BrowserAuthenticatorTypes.M_ACCESS_WEB;
import static org.apereo.cas.configuration.model.support.mfa.InweboMultifactorAuthenticationProperties.BrowserAuthenticatorTypes.VIRTUAL_AUTHENTICATOR;
import static org.apereo.cas.support.inwebo.web.flow.actions.InweboWebflowConstants.*;

/**
 * A web action to check the user (status).
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Slf4j
public class InweboCheckUserAction extends BaseCasWebflowAction {

    private final InweboService service;

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val login = authentication.getPrincipal().getId();
        LOGGER.trace("Login: [{}]", login);

        val flowScope = requestContext.getFlowScope();
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        flowScope.put(SITE_ALIAS, inwebo.getSiteAlias());
        flowScope.put(SITE_DESCRIPTION, inwebo.getSiteDescription());
        flowScope.put(LOGIN, login);
        val browserAuthenticator = inwebo.getBrowserAuthenticator();
        val isVirtualAuthenticator = browserAuthenticator == VIRTUAL_AUTHENTICATOR;
        val isMAccessWeb = browserAuthenticator == M_ACCESS_WEB;
        if (isVirtualAuthenticator) {
            flowScope.put(BROWSER_AUTHENTICATOR, VA);
        } else if (isMAccessWeb) {
            flowScope.put(BROWSER_AUTHENTICATOR, MA);
        }
        val pushEnabled = inwebo.isPushEnabled();
        val pushAuto = inwebo.isPushAuto();

        try {
            val response = service.loginSearchQuery(login);
            val oneUser = response.isOk() && response.getCount() == 1 && response.getUserId() > 0;
            if (oneUser) {
                val userIsBlocked = response.getUserStatus() == 1;
                if (userIsBlocked) {
                    LOGGER.error("User is blocked: [{}]", login);
                    return error();
                }
                var activationStatus = response.getActivationStatus();

                if (activationStatus == 1 && !pushAuto) {
                    activationStatus = PUSH_AND_BROWSER_AUTHENTICATION_STATUS;
                }

                if (activationStatus == 0) {
                    LOGGER.debug("User is not registered: [{}]", login);
                    if (isVirtualAuthenticator) {
                        return customEvent(VA);
                    } else if (isMAccessWeb) {
                        flowScope.put(MUST_ENROLL, true);
                        WebUtils.addErrorMessageToContext(requestContext, "cas.inwebo.error.usernotregistered");
                    }

                } else if (activationStatus == 1) {
                    LOGGER.debug("User can only handle push notifications: [{}]", login);
                    if (pushEnabled) {
                        return customEvent(PUSH);
                    }

                } else if (activationStatus == 2 || activationStatus == 3) {
                    LOGGER.warn("Unexpected activationStatus: [{}]", activationStatus);

                } else if (activationStatus == BROWSER_AUTHENTICATION_STATUS) {
                    LOGGER.debug("User can only handle browser authentication: [{}]", login);
                    if (isVirtualAuthenticator) {
                        return customEvent(VA);
                    } else if (isMAccessWeb) {
                        return customEvent(MA);
                    }

                } else if (activationStatus == PUSH_AND_BROWSER_AUTHENTICATION_STATUS) {
                    LOGGER.debug("User has both authentication methods: [{}]", login);
                    if (pushEnabled) {
                        if (isVirtualAuthenticator || isMAccessWeb) {
                            return customEvent(SELECT);
                        }
                        return customEvent(PUSH);
                    } else {
                        if (isVirtualAuthenticator) {
                            return customEvent(VA);
                        } else if (isMAccessWeb) {
                            return customEvent(MA);
                        }
                    }
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

    protected Event customEvent(final String event) {
        return eventFactory.event(this, event);
    }
}
