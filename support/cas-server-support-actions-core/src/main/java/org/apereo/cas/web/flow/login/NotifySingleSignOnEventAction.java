package org.apereo.cas.web.flow.login;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;

/**
 * This is {@link NotifySingleSignOnEventAction}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class NotifySingleSignOnEventAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;

    private final CommunicationsManager communicationsManager;

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        if (casProperties.getSso().getMail().isUndefined() && casProperties.getSso().getSms().isUndefined()) {
            LOGGER.debug("Communication settings for email/sms are undefined for single sign-on notifications. Skipping...");
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_SKIP);
        }
        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(requestContext);
        val ticketGrantingTicket = ticketRegistry.getTicket(ticketGrantingTicketId);
        if (!(ticketGrantingTicket instanceof final AuthenticationAwareTicket aat)) {
            LOGGER.debug("No ticket-granting ticket is found in the context for single sign-on notification events");
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_SKIP);
        }

        if (casProperties.getSso().getMail().isDefined()) {
            sendSingleSignOnEventEmail(requestContext, aat);
        }
        if (casProperties.getSso().getSms().isDefined()) {
            sendSingleSignOnEventSms(requestContext, aat);
        }
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }

    protected void sendSingleSignOnEventSms(final RequestContext context, final AuthenticationAwareTicket aat) {
        val message = SmsBodyBuilder.builder()
            .properties(casProperties.getSso().getSms())
            .parameters(buildCommunicationParameters(context, aat))
            .build()
            .get();
        casProperties.getSso().getSms().getAttributeName()
            .forEach(attribute -> {
                val smsRequest = SmsRequest.builder()
                    .from(casProperties.getSso().getSms().getFrom())
                    .text(message)
                    .principal(aat.getAuthentication().getPrincipal())
                    .attribute(attribute)
                    .tenant(ClientInfoHolder.getClientInfo().getTenant())
                    .build();
                LOGGER.debug("Attempting to send SMS [{}] to [{}]", message, smsRequest.getRecipients());
                communicationsManager.sms(smsRequest);
            });
    }

    protected void sendSingleSignOnEventEmail(final RequestContext context, final AuthenticationAwareTicket aat) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val body = EmailMessageBodyBuilder.builder()
            .properties(casProperties.getSso().getMail())
            .parameters(buildCommunicationParameters(context, aat))
            .build()
            .get();
        casProperties.getSso().getMail().getAttributeName()
            .forEach(attribute -> {
                val emailRequest = EmailMessageRequest.builder()
                    .emailProperties(casProperties.getSso().getMail())
                    .tenant(clientInfo.getTenant())
                    .principal(aat.getAuthentication().getPrincipal())
                    .attribute(attribute)
                    .body(body)
                    .build();
                LOGGER.debug("Attempting to send email [{}] to [{}]", body, emailRequest.getRecipients());
                communicationsManager.email(emailRequest);
            });
    }

    protected Map<String, Object> buildCommunicationParameters(final RequestContext context,
                                                               final AuthenticationAwareTicket aat) {
        return Map.of(
            "context", context,
            "ticketGrantingTicket", aat.getId(),
            "clientInfo", ClientInfoHolder.getClientInfo(),
            "authentication", aat.getAuthentication(),
            "principal", aat.getAuthentication().getPrincipal(),
            "principalId", aat.getAuthentication().getPrincipal().getId()
        );
    }

}
     
