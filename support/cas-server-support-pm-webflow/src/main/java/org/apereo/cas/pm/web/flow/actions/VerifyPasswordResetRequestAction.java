package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
public class VerifyPasswordResetRequestAction extends BasePasswordManagementAction {
    /**
     * Event id to signal security questions are disabled.
     */
    public static final String EVENT_ID_SECURITY_QUESTIONS_DISABLED = "questionsDisabled";

    private final CasConfigurationProperties casProperties;

    private final PasswordManagementService passwordManagementService;

    private final CentralAuthenticationService centralAuthenticationService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val transientTicket = request.getParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN);

        if (StringUtils.isBlank(transientTicket)) {
            LOGGER.error("Password reset token is missing");
            return error();
        }

        var passwordResetTicket = (TransientSessionTicket) null;
        try {
            passwordResetTicket = centralAuthenticationService.getTicket(transientTicket, TransientSessionTicket.class);
            TicketState.class.cast(passwordResetTicket).update();

            val token = passwordResetTicket.getProperties().get(PasswordManagementWebflowUtils.FLOWSCOPE_PARAMETER_NAME_TOKEN).toString();
            val username = passwordManagementService.parseToken(token);

            val query = PasswordManagementQuery.builder().username(username).build();
            PasswordManagementWebflowUtils.putPasswordResetToken(requestContext, token);
            val pm = casProperties.getAuthn().getPm();
            if (pm.getReset().isSecurityQuestionsEnabled()) {
                val questions = canonicalizeSecurityQuestions(passwordManagementService.getSecurityQuestions(query));
                if (questions.isEmpty()) {
                    LOGGER.warn("No security questions could be found for [{}]", username);
                    return error();
                }
                PasswordManagementWebflowUtils.putPasswordResetSecurityQuestions(requestContext, questions);
            } else {
                LOGGER.debug("Security questions are not enabled");
            }

            PasswordManagementWebflowUtils.putPasswordResetUsername(requestContext, username);
            PasswordManagementWebflowUtils.putPasswordResetSecurityQuestionsEnabled(requestContext,
                pm.getReset().isSecurityQuestionsEnabled());

            if (pm.getReset().isSecurityQuestionsEnabled()) {
                LOGGER.trace("Security questions are enabled; proceeding...");
                return success();
            }
            return new EventFactorySupport().event(this, EVENT_ID_SECURITY_QUESTIONS_DISABLED);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, "Password reset token could not be located or verified", e);
            return error();
        } finally {
            if (passwordResetTicket != null && passwordResetTicket.isExpired()) {
                centralAuthenticationService.deleteTicket(passwordResetTicket);
            }
        }
    }
}
