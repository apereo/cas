package org.apereo.cas.notifications.mail;

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
}
