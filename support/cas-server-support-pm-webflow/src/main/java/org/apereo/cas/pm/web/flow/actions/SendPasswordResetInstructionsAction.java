package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.web.util.UriUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link SendPasswordResetInstructionsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SendPasswordResetInstructionsAction extends AbstractAction {

    /**
     * The CAS configuration properties.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * The communication manager for SMS/emails.
     */
    protected final CommunicationsManager communicationsManager;

    /**
     * The password management service.
     */
    protected final PasswordManagementService passwordManagementService;

    /**
     * Ticket registry instance to hold onto the token.
     */
    protected final TicketRegistry ticketRegistry;

    /**
     * Ticket factory instance.
     */
    protected final TicketFactory ticketFactory;

    /**
     * Utility method to generate a password reset URL.
     *
     * @param username                  username
     * @param passwordManagementService passwordManagementService
     * @param casProperties             casProperties
     * @param service                   service from the flow scope
     * @return URL a user can use to start the password reset process
     */
    public String buildPasswordResetUrl(final String username,
                                        final PasswordManagementService passwordManagementService,
                                        final CasConfigurationProperties casProperties,
                                        final WebApplicationService service) {
        val token = passwordManagementService.createToken(username);
        if (StringUtils.isNotBlank(token)) {
            val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
            val pm = casProperties.getAuthn().getPm();
            val expirationSeconds = TimeUnit.MINUTES.toSeconds(pm.getReset().getExpirationMinutes());
            val properties = CollectionUtils.<String, Serializable>wrap(
                PasswordManagementWebflowUtils.FLOWSCOPE_PARAMETER_NAME_TOKEN, token,
                ExpirationPolicy.class.getName(), HardTimeoutExpirationPolicy.builder().timeToKillInSeconds(expirationSeconds).build());
            val ticket = transientFactory.create(service, properties);
            this.ticketRegistry.addTicket(ticket);
            
            StringBuilder resetUrl = new StringBuilder(casProperties.getServer().getPrefix())
                .append('/').append(CasWebflowConfigurer.FLOW_ID_LOGIN).append('?')
                .append(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN).append('=').append(ticket.getId());

            if (service != null) {
                val encodeServiceUrl = UriUtils.encode(service.getOriginalUrl(), StandardCharsets.UTF_8);
                resetUrl.append('&').append(CasProtocolConstants.PARAMETER_SERVICE).append('=').append(encodeServiceUrl);
            }

            val url = resetUrl.toString();
            LOGGER.debug("Final password reset URL designed for [{}] is [{}]", username, url);
            return url;
        }
        LOGGER.error("Could not create password reset url since no reset token could be generated");
        return null;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        communicationsManager.validate();
        if (!communicationsManager.isMailSenderDefined() && !communicationsManager.isSmsSenderDefined()) {
            return getErrorEvent("contact.failed", "Unable to send email as no mail sender is defined", requestContext);
        }

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val username = request.getParameter("username");

        if (StringUtils.isBlank(username)) {
            LOGGER.warn("No username parameter is provided");
            return getErrorEvent("username.required", "No username is provided", requestContext);
        }

        val email = passwordManagementService.findEmail(username);
        val phone = passwordManagementService.findPhone(username);
        if (StringUtils.isBlank(email) && StringUtils.isBlank(phone)) {
            LOGGER.warn("No recipient is provided with a valid email/phone");
            return getErrorEvent("contact.invalid", "Provided email address or phone number is invalid", requestContext);
        }

        val service = WebUtils.getService(requestContext);
        val url = buildPasswordResetUrl(username, passwordManagementService, casProperties, service);
        if (StringUtils.isNotBlank(url)) {
            val pm = casProperties.getAuthn().getPm();
            LOGGER.debug("Generated password reset URL [{}]; Link is only active for the next [{}] minute(s)", url, pm.getReset().getExpirationMinutes());
            if (sendPasswordResetEmailToAccount(email, url) || sendPasswordResetSmsToAccount(phone, url)) {
                return success();
            }
        } else {
            LOGGER.error("No password reset URL could be built and sent to [{}]", email);
        }
        LOGGER.error("Failed to notify account [{}]", email);
        return getErrorEvent("contact.failed", "Failed to send the password reset link to the given email address or phone number", requestContext);
    }

    /**
     * Send password reset sms to account.
     *
     * @param to  the to
     * @param url the url
     * @return true/false
     */
    protected boolean sendPasswordResetSmsToAccount(final String to, final String url) {
        if (StringUtils.isNotBlank(to)) {
            LOGGER.debug("Sending password reset URL [{}] via SMS to [{}]", url, to);
            val reset = casProperties.getAuthn().getPm().getReset().getSms();
            val message = reset.getFormattedText(url);
            return communicationsManager.sms(reset.getFrom(), to, message);
        }
        return false;
    }

    /**
     * Send password reset email to account.
     *
     * @param to  the to
     * @param url the url
     * @return true/false
     */
    protected boolean sendPasswordResetEmailToAccount(final String to, final String url) {
        if (StringUtils.isNotBlank(to)) {
            val reset = casProperties.getAuthn().getPm().getReset().getMail();
            val text = reset.getFormattedBody(url);
            LOGGER.debug("Sending password reset URL [{}] via email to [{}]", url, to);
            return this.communicationsManager.email(reset, to, text);
        }
        return false;
    }

    /**
     * Gets error event.
     *
     * @param code           the code
     * @param defaultMessage the default message
     * @param requestContext the request context
     * @return the error event
     */
    protected Event getErrorEvent(final String code, final String defaultMessage, final RequestContext requestContext) {
        val messages = requestContext.getMessageContext();
        messages.addMessage(new MessageBuilder()
            .error()
            .code("screen.pm.reset." + code)
            .defaultText(defaultMessage)
            .build());
        LOGGER.error(defaultMessage);
        return new EventFactorySupport().event(this, CasWebflowConstants.VIEW_ID_ERROR);
    }
}
