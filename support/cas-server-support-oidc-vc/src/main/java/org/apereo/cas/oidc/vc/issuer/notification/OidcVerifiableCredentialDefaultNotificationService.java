package org.apereo.cas.oidc.vc.issuer.notification;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;

/**
 * Registers notification ids as transient session tickets that carry the client, the user and the credential
 * configuration of the credential response, and expire with the access token that obtained the credentials.
 * Notifications are recorded in the audit log.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcVerifiableCredentialDefaultNotificationService implements OidcVerifiableCredentialNotificationService {
    private static final String PROPERTY_PRINCIPAL = "verifiableCredentialNotificationPrincipal";

    private static final String PROPERTY_CREDENTIAL_CONFIGURATION_ID = "verifiableCredentialNotificationConfigurationId";

    private final OidcConfigurationContext configurationContext;

    @Override
    public String register(final OAuth20AccessToken accessToken, final String credentialConfigurationId) throws Throwable {
        val properties = new HashMap<String, Serializable>();
        properties.put(OAuth20Constants.CLIENT_ID, accessToken.getClientId());
        properties.put(PROPERTY_PRINCIPAL, accessToken.getAuthentication().getPrincipal().getId());
        properties.put(PROPERTY_CREDENTIAL_CONFIGURATION_ID, credentialConfigurationId);
        properties.put(ExpirationPolicy.class.getName(),
            new HardTimeoutExpirationPolicy(Math.max(1, accessToken.getExpirationPolicy().getTimeToLive())));
        val factory = (TransientSessionTicketFactory) configurationContext.getTicketFactory().get(TransientSessionTicket.class);
        val ticket = Objects.requireNonNull(configurationContext.getTicketRegistry().addTicket(factory.create(properties)));
        return ticket.getId();
    }

    @Audit(action = AuditableActions.OIDC_VERIFIABLE_CREDENTIAL_NOTIFICATION,
        actionResolverName = AuditActionResolvers.OIDC_VERIFIABLE_CREDENTIAL_NOTIFICATION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OIDC_VERIFIABLE_CREDENTIAL_NOTIFICATION_RESOURCE_RESOLVER)
    @Override
    public Notification notify(final OAuth20AccessToken accessToken, final NotificationRequest request) {
        val ticket = FunctionUtils.doAndHandle(() -> configurationContext.getTicketRegistry()
            .getTicket(request.notificationId(), TransientSessionTicket.class));
        val credentialConfigurationId = ticket != null ? ticket.getProperty(PROPERTY_CREDENTIAL_CONFIGURATION_ID, String.class) : null;
        if (ticket == null || ticket.isExpired() || credentialConfigurationId == null
            || !accessToken.getClientId().equals(ticket.getProperty(OAuth20Constants.CLIENT_ID, String.class))
            || !accessToken.getAuthentication().getPrincipal().getId().equals(ticket.getProperty(PROPERTY_PRINCIPAL, String.class))) {
            throw new InvalidNotificationException("Notification id is unknown, expired or was not issued to this client");
        }
        val notification = new Notification(request.notificationId(), accessToken.getClientId(),
            credentialConfigurationId, request.event(), request.eventDescription());
        LOGGER.info("Received credential notification [{}]", notification);
        return notification;
    }
}
