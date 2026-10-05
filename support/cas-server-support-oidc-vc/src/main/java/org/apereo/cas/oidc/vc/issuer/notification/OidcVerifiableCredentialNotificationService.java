package org.apereo.cas.oidc.vc.issuer.notification;

import module java.base;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.jspecify.annotations.Nullable;

/**
 * Keeps track of the {@code notification_id} values handed out with credential responses and receives the
 * notifications wallets send about them, per OpenID4VCI 1.0 section 11.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OidcVerifiableCredentialNotificationService {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcVerifiableCredentialNotificationService";

    /**
     * Notification events a wallet may report.
     */
    Set<String> EVENTS = Set.of("credential_accepted", "credential_failure", "credential_deleted");

    /**
     * Register a notification id for a credential response, bound to the client and the user of the access token.
     *
     * @param accessToken               the access token that obtained the credentials
     * @param credentialConfigurationId the credential configuration of the issued credentials
     * @return the notification id
     * @throws Throwable the throwable
     */
    String register(OAuth20AccessToken accessToken, String credentialConfigurationId) throws Throwable;

    /**
     * Receive a notification. The notification id must have been registered for the client and the user of the
     * access token and must not have expired; receiving the same notification again succeeds again.
     *
     * @param accessToken the access token presented with the notification
     * @param request     the notification request
     * @return the notification
     * @throws InvalidNotificationException when the notification id is unknown, expired or not the caller's
     */
    Notification notify(OAuth20AccessToken accessToken, NotificationRequest request);

    /**
     * A notification request, as sent by the wallet.
     *
     * @param notificationId   the notification id
     * @param event            the event
     * @param eventDescription the event description, if any
     */
    record NotificationRequest(String notificationId, String event, @Nullable String eventDescription) {
    }

    /**
     * A received notification.
     *
     * @param notificationId            the notification id
     * @param clientId                  the client that received the credentials
     * @param credentialConfigurationId the credential configuration of the credentials
     * @param event                     the event
     * @param eventDescription          the event description, if any
     */
    record Notification(String notificationId, String clientId, String credentialConfigurationId,
                        String event, @Nullable String eventDescription) {
    }

    /**
     * Thrown when a notification id is unknown, expired or not the caller's.
     */
    class InvalidNotificationException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 4931580616306475162L;

        public InvalidNotificationException(final String message) {
            super(message);
        }
    }
}
