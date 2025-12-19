package org.apereo.cas.mail;

import module java.base;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.util.function.FunctionUtils;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGridAPI;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * This is {@link SendGridEmailSender}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailSender implements EmailSender {
    private final SendGridAPI sendGridAPI;

    private final MessageSource messageSource;

    @Override
    public EmailCommunicationResult send(final EmailMessageRequest emailRequest) {
        val results = emailRequest.getRecipients()
            .stream()
            .map(recipient -> FunctionUtils.doAndHandle(() -> {
                val response = sendEmailWithSendGrid(emailRequest, recipient);
                LOGGER.debug("SendGrid response [{}]:[{}]", response.getStatusCode(), response.getBody());
                return EmailCommunicationResult.builder()
                    .success(HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful())
                    .to(List.of(recipient))
                    .body(response.getBody())
                    .details(response.getHeaders())
                    .build();
            },
                e -> EmailCommunicationResult.builder().success(false).body(e.getMessage()).build()).get())
            .toList();

        return results
            .stream()
            .filter(r -> !r.isSuccess())
            .findFirst()
            .orElseGet(() -> EmailCommunicationResult.builder().success(true).build());
    }

    protected Response sendEmailWithSendGrid(final EmailMessageRequest emailRequest,
                                             final String recipient) throws Exception {
        val request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(buildRequestBody(emailRequest, recipient));
        return sendGridAPI.api(request);
    }

    protected String buildRequestBody(final EmailMessageRequest emailRequest,
                                      final String recipient) throws Exception {
        val from = new Email(emailRequest.getEmailProperties().getFrom());
        val subject = determineEmailSubject(emailRequest, messageSource);

        val to = new Email(recipient);
        val type = emailRequest.getEmailProperties().isHtml()
            ? MediaType.TEXT_PLAIN_VALUE
            : MediaType.TEXT_HTML_VALUE;
        val content = new Content(type, emailRequest.getBody());
        val mail = new Mail(from, subject, to, content);
        return mail.build();
    }
}
