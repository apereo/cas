package org.apereo.cas.notifications;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * This is {@link DefaultCommunicationsManager}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCommunicationsManager implements CommunicationsManager {
    private final SmsSender smsSender;

    private final JavaMailSender mailSender;

    private final NotificationSender notificationSender;

    private final HierarchicalMessageSource messageSource;

    @Override
    public boolean isMailSenderDefined() {
        return this.mailSender != null;
    }

    @Override
    public boolean isSmsSenderDefined() {
        return this.smsSender != null && this.smsSender.canSend();
    }

    @Override
    public boolean isNotificationSenderDefined() {
        return this.notificationSender != null && this.notificationSender.canSend();
    }

    @Override
    public boolean notify(final Principal principal, final String title, final String body) {
        return this.notificationSender.notify(principal, Map.of("title", title, "message", body));
    }

    @Override
    public EmailCommunicationResult email(final EmailMessageRequest emailRequest) {
        val recipients = emailRequest.getRecipients();
        try {
            LOGGER.trace("Attempting to send email [{}] to [{}]", emailRequest.getBody(), recipients);
            if (!isMailSenderDefined() || emailRequest.getEmailProperties().isUndefined() || recipients.isEmpty()) {
                throw new IllegalAccessException("Could not send email; from/to/subject/text or email settings are undefined.");
            }

            val message = mailSender.createMimeMessage();
            val helper = new MimeMessageHelper(message);
            helper.setTo(recipients.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            helper.setText(emailRequest.getBody(), emailRequest.getEmailProperties().isHtml());

            val subject = determineEmailSubject(emailRequest);
            helper.setSubject(subject);

            helper.setFrom(emailRequest.getEmailProperties().getFrom());
            if (StringUtils.isNotBlank(emailRequest.getEmailProperties().getReplyTo())) {
                helper.setReplyTo(emailRequest.getEmailProperties().getReplyTo());
            }
            helper.setValidateAddresses(emailRequest.getEmailProperties().isValidateAddresses());
            helper.setPriority(emailRequest.getEmailProperties().getPriority());
            helper.setCc(emailRequest.getEmailProperties().getCc().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            helper.setBcc(emailRequest.getEmailProperties().getBcc().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            mailSender.send(message);
            return EmailCommunicationResult.builder().success(true)
                .to(recipients).body(emailRequest.getBody()).build();
        } catch (final Exception ex) {
            LoggingUtils.error(LOGGER, ex);
        }
        return EmailCommunicationResult.builder().success(false)
            .to(recipients).body(emailRequest.getBody()).build();
    }

    protected String determineEmailSubject(final EmailMessageRequest emailRequest) {
        var subject = emailRequest.getEmailProperties().getSubject();
        val pattern = RegexUtils.createPattern("#\\{(.+)\\}");
        val matcher = pattern.matcher(subject);
        if (matcher.find()) {
            val args = new ArrayList<>();
            if (emailRequest.getPrincipal() != null) {
                args.add(emailRequest.getPrincipal().getId());
            }
            return messageSource.getMessage(matcher.group(1), args.toArray(),
                "Email Subject", ObjectUtils.defaultIfNull(emailRequest.getLocale(), Locale.getDefault()));
        }
        return SpringExpressionLanguageValueResolver.getInstance().resolve(subject);
    }

    @Override
    public boolean sms(final SmsRequest smsRequest) {
        val recipient = smsRequest.getRecipient();
        if (!isSmsSenderDefined() || !smsRequest.isSufficient()) {
            LOGGER.warn("Could not send SMS to [{}]; No from/text is found or SMS settings are undefined.", recipient);
            return false;
        }
        return smsSender.send(smsRequest.getFrom(), recipient, smsRequest.getText());
    }

    @Override
    public boolean validate() {
        if (!isMailSenderDefined()) {
            LOGGER.info("CAS will not send emails because settings are undefined to account for email servers");
        }
        if (!isSmsSenderDefined()) {
            LOGGER.info("CAS will not send sms messages because settings are undefined to account for sms providers");
        }
        if (!isNotificationSenderDefined()) {
            LOGGER.info("CAS will not send notifications because providers are undefined to handle messages");
        }
        return isMailSenderDefined() || isSmsSenderDefined() || isNotificationSenderDefined();
    }

}
