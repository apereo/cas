package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.configuration.model.support.mfa.CasSimpleMultifactorProperties;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
    private final TicketRegistry ticketRegistry;
    private final CommunicationsManager communicationsManager;
    private final TransientSessionTicketFactory ticketFactory;
    private final CasSimpleMultifactorProperties properties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val service = WebUtils.getService(requestContext);
        val token = ticketFactory.create(service);

        val authentication = WebUtils.getInProgressAuthentication();
        val principal = authentication.getPrincipal();

        val smsProperties = properties.getSms();
        val text = StringUtils.isNotBlank(smsProperties.getText())
            ? smsProperties.getFormattedText(token.getId())
            : token.getId();

        val emailProperties = properties.getMail();
        val body = emailProperties.getFormattedBody(token.getId());

        val smsSent = communicationsManager.sms(principal, smsProperties.getAttributeName(), text, smsProperties.getFrom());
        val emailSent = communicationsManager.email(principal, emailProperties.getAttributeName(), emailProperties, body);

        if (smsSent || emailSent) {
            ticketRegistry.addTicket(token);
            LOGGER.debug("Successfully submitted token via SMS and/or email to [{}]", principal.getId());
            val attributes = new LocalAttributeMap("token", token.getId());
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS, attributes);
        }
        throw new UnauthorizedAuthenticationException("Both email and SMS communication strategies failed to submit token to user");
    }
}
