package org.apereo.cas.notifications;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.notifications.call.PhoneCallRequest;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsRequest;
import lombok.val;
import java.util.List;

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
     * @return true/false
     */
    boolean isMailSenderDefined();

    /**
     * Is sms sender defined?
     *
     * @return true/false
     */
    boolean isSmsSenderDefined();

    /**
     * Is notification sender defined?
     *
     * @return true/false
     */
    boolean isNotificationSenderDefined();

    /**
     * Is phone operator defined?.
     *
     * @return true/false
     */
    boolean isPhoneOperatorDefined();

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
     * @param emailRequest the email request
     * @return the result
     */
    EmailCommunicationResult email(EmailMessageRequest emailRequest);

    /**
     * Sms.
     *
     * @param request the request
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean sms(SmsRequest request) throws Throwable;

    /**
     * Sms multiple requests.
     *
     * @param requests the requests
     * @return true/false
     * @throws Throwable the throwable
     */
    default boolean sms(final List<SmsRequest> requests) throws Throwable {
        for (val request : requests) {
            if (sms(request)) {
                return true;
            }
        }
        return false;
    }

    /**
     * make a phone call.
     *
     * @param request the request
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean phoneCall(PhoneCallRequest request) throws Throwable;

    /**
     * Validate.
     *
     * @return true, if email or sms providers, etc are defined for CAS.
     */
    boolean validate();
}
