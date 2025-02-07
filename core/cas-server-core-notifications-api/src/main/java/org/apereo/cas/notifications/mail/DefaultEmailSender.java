package org.apereo.cas.notifications.mail;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * This is {@link DefaultEmailSender}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultEmailSender implements EmailSender {
    private final JavaMailSender mailSender;

    private final MessageSource messageSource;

    private final ApplicationContext applicationContext;

    @Override
    public boolean canSend() {
        try {
            if (mailSender != null && mailSender instanceof final JavaMailSenderImpl impl) {
                impl.testConnection();
                return true;
            }
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public EmailCommunicationResult send(final EmailMessageRequest emailRequest) throws Exception {
        val recipients = emailRequest.getRecipients();
        val message = mailSender.createMimeMessage();
        val messageHelper = new MimeMessageHelper(message);
        messageHelper.setTo(recipients.toArray(ArrayUtils.EMPTY_STRING_ARRAY));

        val emailProperties = emailRequest.getEmailProperties();
        messageHelper.setText(emailRequest.getBody(), emailProperties.isHtml());

        val subject = determineEmailSubject(emailRequest, messageSource);
        messageHelper.setSubject(subject);

        messageHelper.setFrom(emailProperties.getFrom());
        FunctionUtils.doIfNotBlank(emailProperties.getReplyTo(), __ -> messageHelper.setReplyTo(emailProperties.getReplyTo()));
        messageHelper.setValidateAddresses(emailProperties.isValidateAddresses());
        messageHelper.setPriority(emailProperties.getPriority());
        messageHelper.setCc(emailProperties.getCc().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        messageHelper.setBcc(emailProperties.getBcc().toArray(ArrayUtils.EMPTY_STRING_ARRAY));

        applicationContext.getBeansOfType(EmailSenderCustomizer.class, false, false)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(customizer -> customizer.customize(mailSender, emailRequest));

        mailSender.send(message);

        return EmailCommunicationResult.builder()
            .success(true)
            .to(recipients)
            .body(emailRequest.getBody())
            .build();
    }

}
