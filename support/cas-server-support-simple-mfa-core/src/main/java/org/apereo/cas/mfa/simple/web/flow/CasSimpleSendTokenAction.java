package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.CasSimpleMultifactorProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CasSimpleSendTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasSimpleSendTokenAction extends AbstractAction {
    private static final String MESSAGE_MFA_TOKEN_SENT = "cas.mfa.simple.label.tokensent";

    private final TicketRegistry ticketRegistry;
    private final CommunicationsManager communicationsManager;
    private final TransientSessionTicketFactory ticketFactory;
    private final CasSimpleMultifactorProperties properties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getInProgressAuthentication();
        val principal = authentication.getPrincipal();
        val service = WebUtils.getService(requestContext);
        val token = ticketFactory.create(service, CollectionUtils.wrap(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal));
        LOGGER.debug("Created multifactor authentication token [{}] for service [{}]", token, service);

        val smsSent = isSmsSent(communicationsManager, properties, principal, token);
        val emailSent = isMailSent(communicationsManager, properties, principal, token);

        if (smsSent || emailSent) {
            ticketRegistry.addTicket(token);
            LOGGER.debug("Successfully submitted token via SMS and/or email to [{}]", principal.getId());

            val resolver = new MessageBuilder()
                .info()
                .code(MESSAGE_MFA_TOKEN_SENT)
                .defaultText(MESSAGE_MFA_TOKEN_SENT)
                .build();
            requestContext.getMessageContext().addMessage(resolver);

            val attributes = new LocalAttributeMap("token", token.getId());
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS, attributes);
        }
        LOGGER.error("Both email and SMS communication strategies failed to submit token [{}] to user", token);
        return error();
    }

    private static boolean isSmsSent(final CommunicationsManager communicationsManager,
                                     final CasSimpleMultifactorProperties properties,
                                     final Principal principal,
                                     final Ticket token) {
        if (communicationsManager.isSmsSenderDefined()) {
            val smsProperties = properties.getSms();
            val smsText = StringUtils.isNotBlank(smsProperties.getText())
                ? smsProperties.getFormattedText(token.getId())
                : token.getId();
            return communicationsManager.sms(principal, smsProperties.getAttributeName(), smsText, smsProperties.getFrom());
        }
        return false;
    }

    private static boolean isMailSent(final CommunicationsManager communicationsManager,
                                      final CasSimpleMultifactorProperties properties,
                                      final Principal principal,
                                      final Ticket token) {
        if (communicationsManager.isMailSenderDefined()) {
            val mailProperties = properties.getMail();
            return communicationsManager.email(principal, mailProperties.getAttributeName(), mailProperties, mailProperties.getFormattedBody(token.getId()));
        }
        return false;
    }
}
