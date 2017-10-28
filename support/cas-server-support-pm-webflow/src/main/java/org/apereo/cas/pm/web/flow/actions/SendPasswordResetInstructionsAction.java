package org.apereo.cas.pm.web.flow.actions;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

import static org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer.*;

/**
 * This is {@link SendPasswordResetInstructionsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SendPasswordResetInstructionsAction extends AbstractAction {
    /** Param name for the token. */
    public static final String PARAMETER_NAME_TOKEN = "pswdrst";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SendPasswordResetInstructionsAction.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    private final CommunicationsManager communicationsManager;

    private final PasswordManagementService passwordManagementService;

    public SendPasswordResetInstructionsAction(final CommunicationsManager communicationsManager, 
                                               final PasswordManagementService passwordManagementService) {
        this.communicationsManager = communicationsManager;
        this.passwordManagementService = passwordManagementService;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        if (!communicationsManager.isMailSenderDefined()) {
            LOGGER.warn("CAS is unable to send password-reset emails given no settings are defined to account for email servers");
            return error();
        }
        final PasswordManagementProperties pm = casProperties.getAuthn().getPm();
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final String username = request.getParameter("username");
        if (StringUtils.isBlank(username)) {
            LOGGER.warn("No username is provided");
            return error();
        }

        final String to = passwordManagementService.findEmail(username);
        if (StringUtils.isBlank(to)) {
            LOGGER.warn("No recipient is provided");
            return error();
        }
        
        final String url = buildPasswordResetUrl(username, passwordManagementService, casProperties);
        
        LOGGER.debug("Generated password reset URL [{}]; Link is only active for the next [{}] minute(s)", url,
                pm.getReset().getExpirationMinutes());
        if (sendPasswordResetEmailToAccount(to, url)) {
            return success();
        }
        LOGGER.error("Failed to notify account [{}]", to);
        return error();
    }

    /**
     * Utility method to generate a password reset URL.
     *
     * @param username username
     * @param passwordManagementService passwordManagementService
     * @param casProperties casProperties
     * @return URL a user can use to start the password reset process
     */
    public static String buildPasswordResetUrl(final String username,
            final PasswordManagementService passwordManagementService, final CasConfigurationProperties casProperties) {
        final String token = passwordManagementService.createToken(username);
        return casProperties.getServer().getPrefix()
                .concat('/' + FLOW_ID_LOGIN + '?' + PARAMETER_NAME_TOKEN + '=').concat(token);
    }

    /**
     * Send password reset email to account.
     *
     * @param to  the to
     * @param url the url
     * @return true/false
     */
    protected boolean sendPasswordResetEmailToAccount(final String to, final String url) {
        final PasswordManagementProperties.Reset reset = casProperties.getAuthn().getPm().getReset();
        final String text = String.format(reset.getText(), url);
        return this.communicationsManager.email(text, reset.getFrom(), reset.getSubject(), to, null, null);
    }
}
