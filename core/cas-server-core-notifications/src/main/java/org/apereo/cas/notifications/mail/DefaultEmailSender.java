package org.apereo.cas.notifications.mail;

import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.ArrayList;
import java.util.Locale;

/**
 * This is {@link DefaultEmailSender}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class DefaultEmailSender implements EmailSender {
    private final JavaMailSender mailSender;

    private final HierarchicalMessageSource messageSource;

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

}
