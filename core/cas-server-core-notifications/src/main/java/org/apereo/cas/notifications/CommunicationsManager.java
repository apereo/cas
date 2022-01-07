package org.apereo.cas.notifications;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.email.EmailProperties;

/**
 * This is {@link CommunicationsManager}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface CommunicationsManager {
    /**
     * Bean name.
     */
    String BEAN_NAME = "communicationsManager";

    /**
     * Is mail sender defined?
     *
     * @return the boolean
     */
    boolean isMailSenderDefined();

    /**
     * Is sms sender defined?
     *
     * @return the boolean
     */
    boolean isSmsSenderDefined();

    /**
     * Is notification sender defined?
     *
     * @return the boolean
     */
    boolean isNotificationSenderDefined();

    /**
     * Notify.
     *
     * @param principal the principal
     * @param title     the title
     * @param body      the body
     * @return true/false
     */
    boolean notify(Principal principal, String title, String body);

    /**
     * Email.
     *
     * @param principal       the principal
     * @param attribute       the email attribute
     * @param emailProperties the email properties
     * @param body            the body
     * @return true /false
     */
    boolean email(Principal principal,
                  String attribute,
                  EmailProperties emailProperties,
                  String body);

    /**
     * Email.
     *
     * @param emailProperties the email properties
     * @param to              the to
     * @param body            the body
     * @return true/false
     */
    boolean email(EmailProperties emailProperties, String to, String body);

    /**
     * Sms.
     *
     * @param principal the principal
     * @param attribute the attribute
     * @param text      the text
     * @param from      the from
     * @return true/false
     */
    boolean sms(Principal principal,
                String attribute,
                String text,
                String from);

    /**
     * Sms.
     *
     * @param from the from
     * @param to   the to
     * @param text the text
     * @return true/false
     */
    boolean sms(String from, String to, String text);

    /**
     * Validate.
     *
     * @return true, if email or sms providers, etc are defined for CAS.
     */
    boolean validate();
}
