package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

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

    private final TicketRegistrySupport ticketRegistrySupport;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val transientTicket = request.getParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN);
        try {
            val tgt = WebUtils.getTicketGrantingTicketId(requestContext);

            if (StringUtils.isBlank(transientTicket) && StringUtils.isBlank(tgt)) {
                LOGGER.error("Password reset token is missing");
                return error();
            }

            val username = getUsernameFromTransientTicket(requestContext, transientTicket).orElseGet(() -> {
                val principal = ticketRegistrySupport.getAuthenticatedPrincipalFrom(tgt);
                return principal.getId();
            });
            
            val query = PasswordManagementQuery.builder().username(username).build();
            val pm = casProperties.getAuthn().getPm();
            if (pm.getReset().isSecurityQuestionsEnabled()) {
                val questions = PasswordManagementService.canonicalizeSecurityQuestions(passwordManagementService.getSecurityQuestions(query));
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
            removeTransientTicketIfNecessary(transientTicket);
        }
    }

    private void removeTransientTicketIfNecessary(final String transientTicket) throws Exception {
        if (StringUtils.isNotBlank(transientTicket)) {
            FunctionUtils.doAndHandle(__ -> {
                val passwordResetTicket = ticketRegistrySupport.ticketRegistry().getTicket(transientTicket, TransientSessionTicket.class);
                if (passwordResetTicket != null && passwordResetTicket.getExpirationPolicy().isExpired(passwordResetTicket)) {
                    ticketRegistrySupport.ticketRegistry().deleteTicket(passwordResetTicket);
                }
            });
        }
    }

    private Optional<String> getUsernameFromTransientTicket(final RequestContext requestContext,
                                                            final String transientTicket) {
        return Optional.ofNullable(transientTicket)
            .map(Unchecked.function(__ -> {
                val passwordResetTicket = ticketRegistrySupport.ticketRegistry().getTicket(transientTicket, TransientSessionTicket.class);
                passwordResetTicket.update();
                ticketRegistrySupport.ticketRegistry().updateTicket(passwordResetTicket);
                val token = passwordResetTicket.getProperties().get(PasswordManagementService.PARAMETER_TOKEN).toString();
                PasswordManagementWebflowUtils.putPasswordResetToken(requestContext, token);
                return passwordManagementService.parseToken(token);
            }));
    }
}
