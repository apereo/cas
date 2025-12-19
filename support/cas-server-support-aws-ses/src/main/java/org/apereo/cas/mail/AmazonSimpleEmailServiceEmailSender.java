package org.apereo.cas.mail;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.MessageSource;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

/**
 * This is {@link AmazonSimpleEmailServiceEmailSender}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class AmazonSimpleEmailServiceEmailSender implements EmailSender {
    private final SesClient sesClient;
    private final MessageSource messageSource;
    private final CasConfigurationProperties casProperties;

    @Override
    public EmailCommunicationResult send(final EmailMessageRequest emailRequest) {
        val recipients = emailRequest.getRecipients();

        val destinationBuilder = Destination.builder();
        destinationBuilder.toAddresses(recipients);

        val emailProperties = emailRequest.getEmailProperties();
        destinationBuilder.ccAddresses(emailProperties.getCc());
        destinationBuilder.bccAddresses(emailProperties.getBcc());

        val subject = Content.builder().data(determineEmailSubject(emailRequest, messageSource)).build();
        val body = Body.builder().text(Content.builder().data(emailRequest.getBody()).build()).build();
        val message = Message.builder().body(body).subject(subject).build();

        val emailRequestBuilder = SendEmailRequest
            .builder()
            .destination(destinationBuilder.build())
            .source(emailProperties.getFrom())
            .sourceArn(casProperties.getEmailProvider().getSes().getSourceArn())
            .configurationSetName(casProperties.getEmailProvider().getSes().getConfigurationSetName())
            .message(message);

        FunctionUtils.doIfNotBlank(emailProperties.getReplyTo(),
            _ -> emailRequestBuilder.replyToAddresses(emailProperties.getReplyTo()));

        val sendEmailResult = sesClient.sendEmail(emailRequestBuilder.build());
        return EmailCommunicationResult.builder()
            .success(sendEmailResult.sdkHttpResponse().isSuccessful())
            .to(recipients)
            .body(emailRequest.getBody())
            .details(Map.of("messageId", sendEmailResult.messageId()))
            .build();
    }
}
