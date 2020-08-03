package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link OAuth20DeviceTokenUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@UtilityClass
public class OAuth20DeviceTokenUtils {
    /**
     * Determine expiration policy for service expiration policy.
     *
     * @param servicesManager  the services manager
     * @param expirationPolicy the expiration policy
     * @param service          the service
     * @return the expiration policy
     */
    public static ExpirationPolicy determineExpirationPolicyForService(final ServicesManager servicesManager,
                                                                       final ExpirationPolicyBuilder expirationPolicy,
                                                                       final Service service) {
        val registeredService = servicesManager.findServiceBy(service);
        if (!(registeredService instanceof OAuthRegisteredService)) {
            return expirationPolicy.buildTicketExpirationPolicy();
        }
        val oauthService = OAuthRegisteredService.class.cast(registeredService);
        if (oauthService.getDeviceTokenExpirationPolicy() != null) {
            val policy = oauthService.getDeviceTokenExpirationPolicy();
            val ttl = policy.getTimeToKill();
            if (StringUtils.isNotBlank(ttl)) {
                return new OAuth20DeviceTokenExpirationPolicy(Beans.newDuration(ttl).getSeconds());
            }
        }
        return expirationPolicy.buildTicketExpirationPolicy();
    }
}
