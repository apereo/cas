package org.apereo.cas.pm.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import static org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer.FLOW_ID_PASSWORD_RESET;

/**
 * This is {@link SendPasswordResetInstructionsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SendPasswordResetInstructionsAction extends AbstractAction {
    /** Param name for the token. */
    public static final String PARAMETER_NAME_TOKEN = "t";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SendPasswordResetInstructionsAction.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("mailSender")
    private JavaMailSender mailSender;

    private PasswordManagementService passwordManagementService;

    public SendPasswordResetInstructionsAction(final PasswordManagementService passwordManagementService) {
        this.passwordManagementService = passwordManagementService;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        if (this.mailSender == null) {
            LOGGER.warn("Mail settings are not defined");
            return error();
        }
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
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
        
        final String token = passwordManagementService.createToken(username);
        final String url = casProperties.getServer().getPrefix()
                .concat("/" + FLOW_ID_PASSWORD_RESET + "?" + PARAMETER_NAME_TOKEN + "=").concat(token);
        
        if (sendPasswordResetEmailToAccount(to, url)) {
            return success();
        }
        LOGGER.error("Failed to notify account {}", to);
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
        try {
            final MimeMessage message = this.mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(to);
            final String text = String.format(casProperties.getAuthn().getPm().getReset().getText(), url);
            helper.setText(text);
            helper.setSubject(casProperties.getAuthn().getPm().getReset().getSubject());

            if (StringUtils.isNotBlank(casProperties.getAuthn().getPm().getReset().getFrom())) {
                helper.setFrom(casProperties.getAuthn().getPm().getReset().getFrom());
            }
            helper.setPriority(1);
            this.mailSender.send(message);
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
