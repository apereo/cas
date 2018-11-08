package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link VerifyPasswordResetRequestAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class VerifyPasswordResetRequestAction extends AbstractAction {
    private final CasConfigurationProperties casProperties;
    private final PasswordManagementService passwordManagementService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val pm = casProperties.getAuthn().getPm();

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        LOGGER.debug("Checking for token at param [{}]", SendPasswordResetInstructionsAction.PARAMETER_NAME_TOKEN);
        val token = request.getParameter(SendPasswordResetInstructionsAction.PARAMETER_NAME_TOKEN);

        if (StringUtils.isBlank(token)) {
            LOGGER.error("Password reset token is missing");
            return error();
        }

        val username = passwordManagementService.parseToken(token);
        if (StringUtils.isBlank(username)) {
            LOGGER.error("Password reset token could not be verified");
            return error();
        }

        if (pm.getReset().isSecurityQuestionsEnabled()) {
            val questions = BasePasswordManagementService
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
