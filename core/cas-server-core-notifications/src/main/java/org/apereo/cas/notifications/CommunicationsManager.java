package org.apereo.cas.notifications;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Map;
import java.util.Optional;

/**
 * This is {@link CommunicationsManager}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class CommunicationsManager {
    private final SmsSender smsSender;

    private final JavaMailSender mailSender;

    private final NotificationSender notificationSender;

    public boolean isMailSenderDefined() {
        return this.mailSender != null;
    }

    public boolean isSmsSenderDefined() {
        return this.smsSender != null && this.smsSender.canSend();
    }

    public boolean isNotificationSenderDefined() {
        return this.notificationSender != null && this.notificationSender.canSend();
    }

    /**
     * Notify.
     *
     * @param principal the principal
     * @param title     the title
     * @param body      the body
     * @return true/false
     */
    public boolean notify(final Principal principal, final String title, final String body) {
        return this.notificationSender.notify(principal, Map.of("title", title, "message", body));
    }

    /**
     * Email.
     *
     * @param principal       the principal
     * @param attribute       the email attribute
     * @param emailProperties the email properties
     * @param body            the body
     * @return true /false
     */
    public boolean email(final Principal principal,
        final String attribute,
        final EmailProperties emailProperties,
        final String body) {
        if (StringUtils.isNotBlank(attribute) && principal.getAttributes().containsKey(attribute) && isMailSenderDefined()) {
            val to = getFirstAttributeByName(principal, attribute);
            if (to.isPresent()) {
                return email(emailProperties, to.get().toString(), body);
            }
        }
        LOGGER.debug("Email attribute [{}] cannot be found or no configuration for email provider is defined", attribute);
        return false;
    }

    /**
     * Email.
     *
     * @param emailProperties the email properties
     * @param to              the to
     * @param body            the body
     * @return true/false
     */
    public boolean email(final EmailProperties emailProperties, final String to, final String body) {
        try {
            LOGGER.trace("Attempting to send email [{}] to [{}]", body, to);
            
            if (!isMailSenderDefined() || emailProperties.isUndefined() || StringUtils.isBlank(to)) {
                throw new IllegalAccessException("Could not send email; from/to/subject/text or email settings are undefined.");
            }

            val message = mailSender.createMimeMessage();
            val helper = new MimeMessageHelper(message);
            helper.setTo(to);
            helper.setText(body, emailProperties.isHtml());
            helper.setSubject(emailProperties.getSubject());
            helper.setFrom(emailProperties.getFrom());
            if (StringUtils.isNotBlank(emailProperties.getReplyTo())) {
                helper.setReplyTo(emailProperties.getReplyTo());
            }
            helper.setValidateAddresses(emailProperties.isValidateAddresses());
            helper.setPriority(1);

            if (StringUtils.isNotBlank(emailProperties.getCc())) {
                helper.setCc(emailProperties.getCc());
            }

            if (StringUtils.isNotBlank(emailProperties.getBcc())) {
                helper.setBcc(emailProperties.getBcc());
            }
            this.mailSender.send(message);
            return true;
        } catch (final Exception ex) {
            LoggingUtils.error(LOGGER, ex);
        }
        return false;
    }

    /**
     * Sms.
     *
     * @param principal the principal
     * @param attribute the attribute
     * @param text      the text
     * @param from      the from
     * @return true/false
     */
    public boolean sms(final Principal principal,
        final String attribute,
        final String text, final String from) {
        if (StringUtils.isNotBlank(attribute) && principal.getAttributes().containsKey(attribute) && isSmsSenderDefined()) {
            val to = getFirstAttributeByName(principal, attribute);
            if (to.isPresent()) {
                return sms(from, to.get().toString(), text);
            }
        }
        LOGGER.debug("Phone attribute [{}] cannot be found or no configuration for sms provider is defined", attribute);
        return false;
    }

    /**
     * Sms.
     *
     * @param from the from
     * @param to   the to
     * @param text the text
     * @return true/false
     */
    public boolean sms(final String from, final String to, final String text) {
        if (!isSmsSenderDefined() || StringUtils.isBlank(text) || StringUtils.isBlank(from)) {
            LOGGER.warn("Could not send SMS to [{}] because either no from/text is found or SMS settings are not configured.", to);
            return false;
        }
        return this.smsSender.send(from, to, text);
    }

    /**
     * Validate.
     *
     * @return true, if email or sms providers, etc are defined for CAS.
     */
    public boolean validate() {
        if (!isMailSenderDefined()) {
            LOGGER.info("CAS is unable to send email given no settings are defined to account for email servers, etc");
        }
        if (!isSmsSenderDefined()) {
            LOGGER.info("CAS is unable to send sms messages given no settings are defined to account for sms providers, etc");
        }
        if (!isNotificationSenderDefined()) {
            LOGGER.info("CAS is unable to send notifications given no providers are defined to handle messages, etc");
        }

        return isMailSenderDefined() || isSmsSenderDefined();
    }

    private static Optional<Object> getFirstAttributeByName(final Principal principal, final String attribute) {
        val value = principal.getAttributes().get(attribute);
        return CollectionUtils.firstElement(value);
    }
}
