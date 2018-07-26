package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.configuration.model.support.mfa.CasSimpleMultifactorProperties;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
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
            ? String.format(smsProperties.getText(), token.getId())
            : token.getId();

        if (communicationsManager.sms(principal, smsProperties.getAttributeName(), text, smsProperties.getFrom())) {
            ticketRegistry.addTicket(token);
            LOGGER.debug("Successfully submitted token via SMS to [{}]", principal.getId());
            return success();
        }

        val emailProperties = properties.getMail();
        val body = StringUtils.isNotBlank(emailProperties.getText())
            ? String.format(emailProperties.getText(), token.getId())
            : token.getId();

        if (communicationsManager.email(principal, emailProperties.getAttributeName(), body, emailProperties.getFrom(),
            emailProperties.getSubject(), emailProperties.getCc(), emailProperties.getBcc())) {
            ticketRegistry.addTicket(token);
            LOGGER.debug("Successfully submitted token via SMS to [{}]", principal.getId());
            return success();
        }
        throw new UnauthorizedAuthenticationException("Both email and SMS communication strategies failed to submit token to user");
    }
}
