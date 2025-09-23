package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.pm.web.flow.PasswordResetRequest;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Objects;
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
    private final CasConfigurationProperties casProperties;

    private final PasswordManagementService passwordManagementService;

    private final TicketRegistrySupport ticketRegistrySupport;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        var transientTicket = request.getParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN);
        var resetRequest = PasswordManagementWebflowUtils.getPasswordResetRequest(requestContext);
        if (StringUtils.isBlank(transientTicket) && resetRequest != null && !resetRequest.getPasswordResetTicket().isExpired()) {
            transientTicket = resetRequest.getPasswordResetTicket().getId();
        }
        
        try {
            val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(requestContext);
            if (StringUtils.isBlank(transientTicket) && StringUtils.isBlank(ticketGrantingTicketId)) {
                LOGGER.error("Password reset token is missing");
                return error();
            }
            resetRequest = getPasswordResetRequestFrom(requestContext, transientTicket)
                .orElseGet(() -> getPasswordResetRequestFrom(ticketGrantingTicketId));
            Objects.requireNonNull(resetRequest, "Password reset request cannot be found");
            
            val query = PasswordManagementQuery.builder().username(resetRequest.getUsername()).build();
            val pm = casProperties.getAuthn().getPm();
            if (pm.getReset().isSecurityQuestionsEnabled()) {
                val questions = FunctionUtils.doUnchecked(() -> PasswordManagementService.canonicalizeSecurityQuestions(passwordManagementService.getSecurityQuestions(query)));
                if (questions.isEmpty()) {
                    LOGGER.warn("No security questions could be found for [{}]", resetRequest);
                    return error();
                }
                PasswordManagementWebflowUtils.putPasswordResetSecurityQuestions(requestContext, questions);
            } else {
                LOGGER.debug("Security questions are not enabled for password management");
            }

            PasswordManagementWebflowUtils.putPasswordResetRequest(requestContext, resetRequest);
            PasswordManagementWebflowUtils.putPasswordResetUsername(requestContext, resetRequest.getUsername());
            PasswordManagementWebflowUtils.putPasswordResetSecurityQuestionsEnabled(requestContext, pm.getReset().isSecurityQuestionsEnabled());
            
            if (pm.getReset().isSecurityQuestionsEnabled()) {
                LOGGER.trace("Security questions are enabled; proceeding...");
                return success();
            }
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_SECURITY_QUESTIONS_DISABLED);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, "Password reset token could not be located or verified", e);
            return error();
        } finally {
            removeTransientTicketIfNecessary(resetRequest);
        }
    }

    private void removeTransientTicketIfNecessary(final PasswordResetRequest resetRequest) {
        Optional.ofNullable(resetRequest)
            .map(PasswordResetRequest::getPasswordResetTicket)
            .filter(TicketGrantingTicketAwareTicket.class::isInstance)
            .map(TicketGrantingTicketAwareTicket.class::cast)
            .filter(r -> r.getExpirationPolicy().isExpired(r))
            .ifPresent(token -> FunctionUtils.doAndHandle(__ -> ticketRegistrySupport.getTicketRegistry().deleteTicket(token)));
    }

    private PasswordResetRequest getPasswordResetRequestFrom(final String tgt) {
        val principal = ticketRegistrySupport.getAuthenticatedPrincipalFrom(tgt);
        return PasswordResetRequest.builder().username(principal.getId()).build();
    }
    
    private Optional<PasswordResetRequest> getPasswordResetRequestFrom(final RequestContext requestContext,
                                                                       final String transientTicket) {
        return Optional.ofNullable(transientTicket)
            .map(Unchecked.function(__ -> {
                val ticketRegistry = ticketRegistrySupport.getTicketRegistry();
                val passwordResetTicket = ticketRegistry.getTicket(transientTicket, TransientSessionTicket.class);
                passwordResetTicket.update();
                ticketRegistry.updateTicket(passwordResetTicket);
                val token = passwordResetTicket.getProperties().get(PasswordManagementService.PARAMETER_TOKEN).toString();
                PasswordManagementWebflowUtils.putPasswordResetToken(requestContext, token);
                return PasswordResetRequest.builder()
                    .passwordResetTicket(passwordResetTicket)
                    .username(passwordManagementService.parseToken(token))
                    .build();
            }));
    }
}
