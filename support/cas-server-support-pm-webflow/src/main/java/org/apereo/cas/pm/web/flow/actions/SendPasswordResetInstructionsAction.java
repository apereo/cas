package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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
     * Param name for the token.
     */
    public static final String PARAMETER_NAME_TOKEN = "pswdrst";

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
     * Utility method to generate a password reset URL.
     *
     * @param username                  username
     * @param passwordManagementService passwordManagementService
     * @param casProperties             casProperties
     * @return URL a user can use to start the password reset process
     */
    public static String buildPasswordResetUrl(final String username,
                                               final PasswordManagementService passwordManagementService,
                                               final CasConfigurationProperties casProperties) {
        val token = passwordManagementService.createToken(username);
        if (StringUtils.isNotBlank(token)) {
            return casProperties.getServer().getPrefix()
                .concat('/' + CasWebflowConfigurer.FLOW_ID_LOGIN + '?' + PARAMETER_NAME_TOKEN + '=').concat(token);
        }
        LOGGER.error("Could not create password reset url since no reset token could be generated");
        return null;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        communicationsManager.validate();
        if (!communicationsManager.isMailSenderDefined()) {
            return getErrorEvent("email.failed", "Unable to send email as no mail sender is defined", requestContext);
        }

        val pm = casProperties.getAuthn().getPm();
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val username = request.getParameter("username");

        if (StringUtils.isBlank(username)) {
            LOGGER.warn("No username is provided");
            return getErrorEvent("username.required", "No username is provided", requestContext);
        }

        val to = passwordManagementService.findEmail(username);
        if (StringUtils.isBlank(to)) {
            LOGGER.warn("No recipient is provided");
            return getErrorEvent("email.invalid", "Provided email address is invalid", requestContext);
        }

        val url = buildPasswordResetUrl(username, passwordManagementService, casProperties);
        if (StringUtils.isNotBlank(url)) {
            LOGGER.debug("Generated password reset URL [{}]; Link is only active for the next [{}] minute(s)", url, pm.getReset().getExpirationMinutes());
            if (sendPasswordResetEmailToAccount(to, url)) {
                return success();
            }
        } else {
            LOGGER.error("No password reset URL could be built and sent to [{}]", to);
        }
        LOGGER.error("Failed to notify account [{}]", to);
        return getErrorEvent("email.failed", "Failed to send the password reset link to the given email address", requestContext);
    }

    /**
     * Send password reset email to account.
     *
     * @param to  the to
     * @param url the url
     * @return true/false
     */
    protected boolean sendPasswordResetEmailToAccount(final String to, final String url) {
        val reset = casProperties.getAuthn().getPm().getReset().getMail();
        val text = String.format(reset.getText(), url);
        return this.communicationsManager.email(text, reset.getFrom(),
            reset.getSubject(),
            to,
            reset.getCc(),
            reset.getBcc());
    }

    private Event getErrorEvent(final String code, final String defaultMessage, final RequestContext requestContext) {
        val messages = requestContext.getMessageContext();
        messages.addMessage(new MessageBuilder()
            .error()
            .code("screen.pm.reset." + code)
            .build());
        LOGGER.error(defaultMessage);
        return new EventFactorySupport().event(this, CasWebflowConstants.VIEW_ID_ERROR);
    }
}
