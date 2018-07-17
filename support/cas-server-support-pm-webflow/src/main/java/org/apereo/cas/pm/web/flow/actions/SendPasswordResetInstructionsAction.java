package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
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
        return casProperties.getServer().getPrefix()
            .concat('/' + CasWebflowConfigurer.FLOW_ID_LOGIN + '?' + PARAMETER_NAME_TOKEN + '=').concat(token);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        communicationsManager.validate();
        if (!communicationsManager.isMailSenderDefined()) {
            return error();
        }
        val pm = casProperties.getAuthn().getPm();
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val username = request.getParameter("username");

        if (StringUtils.isBlank(username)) {
            LOGGER.warn("No username is provided");
            return error();
        }

        val to = passwordManagementService.findEmail(username);
        if (StringUtils.isBlank(to)) {
            LOGGER.warn("No recipient is provided");
            return error();
        }

        val url = buildPasswordResetUrl(username, passwordManagementService, casProperties);

        LOGGER.debug("Generated password reset URL [{}]; Link is only active for the next [{}] minute(s)", url,
            pm.getReset().getExpirationMinutes());
        if (sendPasswordResetEmailToAccount(to, url)) {
            return success();
        }
        LOGGER.error("Failed to notify account [{}]", to);
        return error();
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
}
