package org.apereo.cas.pm.web.flow.actions;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsAction.*;

/**
 * This is {@link VerifyPasswordResetRequestAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class VerifyPasswordResetRequestAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyPasswordResetRequestAction.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    private final PasswordManagementService passwordManagementService;

    public VerifyPasswordResetRequestAction(final PasswordManagementService passwordManagementService) {
        this.passwordManagementService = passwordManagementService;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final PasswordManagementProperties pm = casProperties.getAuthn().getPm();

        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        LOGGER.debug("Checking for token at param [{}]", PARAMETER_NAME_TOKEN);
        final String token = request.getParameter(PARAMETER_NAME_TOKEN);

        if (StringUtils.isBlank(token)) {
            LOGGER.error("Password reset token is missing");
            return error();
        }

        final String username = passwordManagementService.parseToken(token);
        if (StringUtils.isBlank(username)) {
            LOGGER.error("Password reset token could not be verified");
            return error();
        }

        if (pm.getReset().isSecurityQuestionsEnabled()) {
            final List<String> questions = BasePasswordManagementService
                    .canonicalizeSecurityQuestions(passwordManagementService.getSecurityQuestions(username));
            if (questions.isEmpty()) {
                LOGGER.warn("No security questions could be found for [{}]", username);
                return error();
            }
            requestContext.getFlowScope().put("questions", questions);
        } else {
            LOGGER.debug("Security questions are not enabled");
        }

        requestContext.getFlowScope().put("token", token);
        requestContext.getFlowScope().put("username", username);
        requestContext.getFlowScope().put("questionsEnabled", pm.getReset().isSecurityQuestionsEnabled());

        if (pm.getReset().isSecurityQuestionsEnabled()) {
            return success();
        }
        return new EventFactorySupport().event(this, "questionsDisabled");
    }
}
