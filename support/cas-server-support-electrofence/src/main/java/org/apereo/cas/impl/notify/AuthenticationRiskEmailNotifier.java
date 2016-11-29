package org.apereo.cas.impl.notify;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.core.authentication.RiskBasedAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;

/**
 * This is {@link AuthenticationRiskEmailNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationRiskEmailNotifier extends BaseAuthenticationRiskNotifier {

    /**
     * Only activated via boot if properties are defined.
     * May not always want to send email.
     */
    @Autowired(required = false)
    @Qualifier("mailSender")
    private JavaMailSender mailSender;

    @Override
    public void publish() {
        final RiskBasedAuthenticationProperties.Response.Mail mail =
                casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMail();

        final Principal principal = authentication.getPrincipal();

        if (this.mailSender == null || StringUtils.isBlank(mail.getText()) || StringUtils.isBlank(mail.getFrom())
                || StringUtils.isBlank(mail.getSubject()) || !principal.getAttributes().containsKey(mail.getAttributeName())) {
            logger.debug("Could not send email {} because either no addresses could be found or email settings are not configured.",
                    principal.getId());
            return;
        }

        try {
            final String to = principal.getAttributes().get(mail.getAttributeName()).toString();
            final MimeMessage message = this.mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(to);
            helper.setText(mail.getText());
            helper.setSubject(mail.getSubject());
            helper.setFrom(mail.getFrom());
            helper.setPriority(1);

            if (StringUtils.isNotBlank(mail.getCc())) {
                helper.setCc(mail.getCc());
            }

            if (StringUtils.isNotBlank(mail.getBcc())) {
                helper.setBcc(mail.getBcc());
            }
            this.mailSender.send(message);
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
