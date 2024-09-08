package org.apereo.cas.notifications.mail;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * This is {@link DefaultEmailSender}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class DefaultEmailSender implements EmailSender {
    private final JavaMailSender mailSender;

    private final MessageSource messageSource;

    @Override
    public boolean canSend() {
        return mailSender != null;
    }

    @Override
    public EmailCommunicationResult send(final EmailMessageRequest emailRequest) throws Exception {
        val recipients = emailRequest.getRecipients();
        val message = mailSender.createMimeMessage();
        val helper = new MimeMessageHelper(message);
        helper.setTo(recipients.toArray(ArrayUtils.EMPTY_STRING_ARRAY));

        val emailProperties = emailRequest.getEmailProperties();
        helper.setText(emailRequest.getBody(), emailProperties.isHtml());

        val subject = determineEmailSubject(emailRequest, messageSource);
        helper.setSubject(subject);

        helper.setFrom(emailProperties.getFrom());
        FunctionUtils.doIfNotBlank(emailProperties.getReplyTo(), __ -> helper.setReplyTo(emailProperties.getReplyTo()));
        helper.setValidateAddresses(emailProperties.isValidateAddresses());
        helper.setPriority(emailProperties.getPriority());
        helper.setCc(emailProperties.getCc().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        helper.setBcc(emailProperties.getBcc().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        mailSender.send(message);
        return EmailCommunicationResult.builder().success(true)
            .to(recipients).body(emailRequest.getBody()).build();
    }
    
}
