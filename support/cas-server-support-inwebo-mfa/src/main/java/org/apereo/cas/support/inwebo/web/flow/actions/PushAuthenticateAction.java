package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.service.response.Result;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * A web action to request a push notification (mobile/desktop).
 *
 * @author Jerome LELEU
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class PushAuthenticateAction extends AbstractAction implements WebflowConstants {

    private final InweboService service;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getInProgressAuthentication();
        val login = authentication.getPrincipal().getId();
        LOGGER.debug("Login: {}", login);
        var response = service.pushAuthenticate(login);
        if (response.getResult() == Result.NOK) {
            response = service.pushAuthenticate(login);
        }
        if (response.isOk()) {
            requestContext.getFlowScope().put(INWEBO_SESSION_ID, response.getSessionId());
            return success();
        }
        return error();
    }
}
