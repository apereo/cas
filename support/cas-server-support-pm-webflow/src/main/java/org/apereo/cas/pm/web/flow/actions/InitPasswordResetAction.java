package org.apereo.cas.pm.web.flow.actions;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitPasswordResetAction}, serves a as placeholder for extensions.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InitPasswordResetAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitPasswordResetAction.class);
    
    private final PasswordManagementService passwordManagementService;

    public InitPasswordResetAction(final PasswordManagementService passwordManagementService) {
        this.passwordManagementService = passwordManagementService;
    }

    
    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final String token = requestContext.getFlowScope().getString("token");

        if (StringUtils.isBlank(token)) {
            LOGGER.error("Password reset token is missing");
            return error();
        }

        final String username = passwordManagementService.parseToken(token);
        if (StringUtils.isBlank(username)) {
            LOGGER.error("Password reset token could not be verified");
            return error();
        }
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        c.setUsername(username);
        WebUtils.putCredential(requestContext, c);
        return success();
    }
}
