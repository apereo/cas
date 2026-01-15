package org.apereo.cas.notifications.mail;

import module java.base;
import org.springframework.core.Ordered;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * This is {@link EmailSenderCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface EmailSenderCustomizer extends Ordered {
    /**
     * Customize email sender.
     *
     * @param mailSender     the mail sender
     * @param messageRequest the message request
     */
    void customize(JavaMailSender mailSender, EmailMessageRequest messageRequest);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
