package org.apereo.cas.mail;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TrackClicks;
import com.mailjet.client.transactional.TrackOpens;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mailjet.client.transactional.response.SendEmailError;
import com.mailjet.client.transactional.response.SentMessageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.MessageSource;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This is {@link MailjetEmailSender}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class MailjetEmailSender implements EmailSender {

    private final MailjetClient mailjetClient;
    private final MessageSource messageSource;
    private final CasConfigurationProperties casProperties;

    @Override
    public EmailCommunicationResult send(final EmailMessageRequest emailRequest) {
        val emailProperties = emailRequest.getEmailProperties();
        val messageBuilder = TransactionalEmail
            .builder()
            .clearBcc()
            .clearCc()
            .clearAttachments()
            .clearVariables()

            .to(emailRequest.getRecipients().stream().map(SendContact::new).collect(Collectors.toSet()))
            .from(new SendContact(emailProperties.getFrom()))
            .bcc(emailProperties.getBcc().stream().map(SendContact::new).collect(Collectors.toSet()))
            .cc(emailProperties.getCc().stream().map(SendContact::new).collect(Collectors.toSet()))
            .subject(determineEmailSubject(emailRequest, messageSource))
            .priority(emailProperties.getPriority())
            .templateLanguage(true)
            .templateErrorReporting(new SendContact(emailProperties.getFrom()))

            .trackClicks(TrackClicks.ENABLED)
            .trackOpens(TrackOpens.ENABLED);

        FunctionUtils.doIfNotBlank(emailProperties.getReplyTo(),
            __ -> messageBuilder.replyTo(new SendContact(emailProperties.getReplyTo())));

        if (emailProperties.isHtml()) {
            messageBuilder.htmlPart(emailRequest.getBody());
        } else {
            messageBuilder.textPart(emailRequest.getBody());
        }

        val request = SendEmailsRequest
            .builder()
            .message(messageBuilder.build())
            .sandboxMode(casProperties.getEmailProvider().getMailjet().isSandboxMode())
            .advanceErrorHandling(true)
            .build();
        var success = false;
        try {
            val sendEmailResult = request.sendWith(mailjetClient);
            success = Arrays.stream(sendEmailResult.getMessages())
                .peek(messageResult -> {
                    if (messageResult.getStatus() == SentMessageStatus.ERROR) {
                        val errors = Arrays.stream(messageResult.getErrors())
                            .map(SendEmailError::getErrorMessage)
                            .collect(Collectors.joining(","));
                        LOGGER.error("Failed to send email: [{}]", errors);
                    }
                })
                .anyMatch(messageResult -> messageResult.getStatus() == SentMessageStatus.SUCCESS);
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
