package org.apereo.cas.notifications.mail;

import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.context.MessageSource;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This is {@link EmailSender}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface EmailSender {

    /**
     * Default bean implementation.
     */
    String BEAN_NAME = "emailSender";

    /**
     * Whether email messages can be sent.
     *
     * @return true/false
     */
    default boolean canSend() {
        return true;
    }

    /**
     * Send email message and report back the result.
     *
     * @param emailRequest the email request
     * @return the email communication result
     * @throws Exception the exception
     */
    EmailCommunicationResult send(EmailMessageRequest emailRequest) throws Exception;

    /**
     * Determine email subject using a given message source.
     *
     * @param emailRequest  the email request
     * @param messageSource the message source
     * @return the string
     */
    default String determineEmailSubject(final EmailMessageRequest emailRequest,
                                         final MessageSource messageSource) {
        val substitutor = new StringSubstitutor(emailRequest.getContext(), "${", "}");
        var subject = substitutor.replace(emailRequest.getEmailProperties().getSubject());

        val pattern = RegexUtils.createPattern("#\\{(.+)\\}");
        val matcher = pattern.matcher(subject);
        if (matcher.find()) {
            val args = new ArrayList<>();
            if (emailRequest.getPrincipal() != null) {
                args.add(emailRequest.getPrincipal().getId());
            }
            return messageSource.getMessage(matcher.group(1), args.toArray(),
                "Email Subject", ObjectUtils.getIfNull(emailRequest.getLocale(), Locale.getDefault()));
        }
        return SpringExpressionLanguageValueResolver.getInstance().resolve(subject);
    }
}
