package org.apereo.cas.mail;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.util.LoggingUtils;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.enums.YesNoHtml;
import com.mailgun.model.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.MessageSource;

/**
 * This is {@link MailgunEmailSender}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class MailgunEmailSender implements EmailSender {

    private final MailgunMessagesApi messagesApi;
    private final MessageSource messageSource;
    private final CasConfigurationProperties casProperties;

    @Override
    public EmailCommunicationResult send(final EmailMessageRequest emailRequest) {
        val emailProperties = emailRequest.getEmailProperties();
        val mailgun = casProperties.getEmailProvider().getMailgun();

        val messageBuilder = Message.builder()
            .from(emailProperties.getFrom())
            .to(emailRequest.getRecipients())
            .testMode(mailgun.isTestMode())
            .replyTo(emailProperties.getReplyTo())
            .tracking(true)
            .trackingOpens(true)
            .trackingClicks(YesNoHtml.YES)
            .subject(determineEmailSubject(emailRequest, messageSource));

        if (emailProperties.getCc() != null && !emailProperties.getCc().isEmpty()) {
            messageBuilder.cc(emailProperties.getCc());
        }
        if (emailProperties.getBcc() != null && !emailProperties.getBcc().isEmpty()) {
            messageBuilder.bcc(emailProperties.getBcc());
        }
        if (emailProperties.isHtml()) {
            messageBuilder.html(emailRequest.getBody());
        } else {
            messageBuilder.text(emailRequest.getBody());
        }

        var success = false;
        try {
            messagesApi.sendMessage(mailgun.getDomain(), messageBuilder.build());
            success = true;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return EmailCommunicationResult.builder()
            .success(success)
            .to(emailRequest.getRecipients())
            .body(emailRequest.getBody())
            .build();
    }
}
