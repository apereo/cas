package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This action {@link ClearWebflowCredentialAction} is invoked ONLY as an exit-action for non-interactive authn flows.
 * Don't clear credentials when {@value CasWebflowConstants#TRANSITION_ID_SUCCESS} occurs which leads the webflow to
 * {@value CasWebflowConstants#STATE_ID_CREATE_TICKET_GRANTING_TICKET} but may be overridden by the AUP flow
 * which needs credentials in some cases.
 * Credentials need to be cleared if webflow is returning to login page where credentials without
 * a username property will not bind correctly to the login form in the thymeleaf template.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

@Slf4j
public class ClearWebflowCredentialAction extends AbstractAction {

    @Override
    @SneakyThrows
    protected Event doExecute(final RequestContext requestContext) {
        val currentEvent = requestContext.getCurrentEvent();
        if (currentEvent == null) {
            return null;
        }
        val current = currentEvent.getId();
        if (current.equalsIgnoreCase(CasWebflowConstants.TRANSITION_ID_SUCCESS)) {
            return null;
        }

        WebUtils.removeCredential(requestContext);
        if (current.equalsIgnoreCase(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE)
            || current.equalsIgnoreCase(CasWebflowConstants.TRANSITION_ID_ERROR)) {
            LOGGER.debug("Current event signaled a failure. Recreating credentials instance from the context");
            WebUtils.createCredential(requestContext);
        }
        return null;
    }
}
