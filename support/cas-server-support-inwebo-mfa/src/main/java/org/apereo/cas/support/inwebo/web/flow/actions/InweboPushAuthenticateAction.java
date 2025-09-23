package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * A web action to request a push notification (mobile/desktop).
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class InweboPushAuthenticateAction extends BaseCasWebflowAction {
    private final InweboService service;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val login = authentication.getPrincipal().getId();
        var response = service.pushAuthenticate(login);
        if (response.getResult() == InweboResult.NOK) {
            response = service.pushAuthenticate(login);
        }
        if (response.isOk()) {
            requestContext.getFlowScope().put(InweboWebflowConstants.INWEBO_SESSION_ID, response.getSessionId());
            return success();
        }
        return error();
    }
}
