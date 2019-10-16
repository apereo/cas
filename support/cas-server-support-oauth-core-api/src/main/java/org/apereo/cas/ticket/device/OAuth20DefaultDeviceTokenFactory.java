package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.RandomUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * Default OAuth device token factory.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class OAuth20DefaultDeviceTokenFactory implements OAuth20DeviceTokenFactory {
    private static final int USER_CODE_LENGTH = 8;

    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator deviceTokenIdGenerator;

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicyBuilder<OAuth20DeviceToken> expirationPolicy;

    /**
     * Length of the generated user code.
     */
    protected final int userCodeLength;


    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;


    public OAuth20DefaultDeviceTokenFactory(final ExpirationPolicyBuilder<OAuth20DeviceToken> expirationPolicy,
                                            final ServicesManager servicesManager) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy, USER_CODE_LENGTH, servicesManager);
    }

    @Override
    public OAuth20DeviceToken createDeviceCode(final Service service) {
        val codeId = deviceTokenIdGenerator.getNewTicketId(OAuth20DeviceToken.PREFIX);
        val expirationPolicyToUse = determineExpirationPolicyForService(service);
        return new OAuth20DefaultDeviceToken(codeId, service, expirationPolicyToUse);
    }

    @Override
    public OAuth20DeviceUserCode createDeviceUserCode(final OAuth20DeviceToken deviceToken) {
        val userCode = generateDeviceUserCode(RandomUtils.randomAlphanumeric(userCodeLength));
        val expirationPolicyToUse = determineExpirationPolicyForService(deviceToken.getService());
        val deviceUserCode = new OAuth20DefaultDeviceUserCode(userCode, deviceToken.getId(), expirationPolicyToUse);
        deviceToken.assignUserCode(deviceUserCode);
        return deviceUserCode;
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }

    @Override
    public String generateDeviceUserCode(final String providedCode) {
        val prefix = OAuth20DeviceUserCode.PREFIX + '-';
        if (providedCode.startsWith(prefix)) {
            return providedCode;
        }
        return prefix + providedCode.toUpperCase();
    }

    private ExpirationPolicy determineExpirationPolicyForService(final Service service) {
        val registeredService = this.servicesManager.findServiceBy(service);
        if (!(registeredService instanceof OAuthRegisteredService)) {
            return this.expirationPolicy.buildTicketExpirationPolicy();
        }
        val oauthService = OAuthRegisteredService.class.cast(registeredService);
        if (oauthService.getDeviceTokenExpirationPolicy() != null) {
            val policy = oauthService.getDeviceTokenExpirationPolicy();
            val ttl = policy.getTimeToKill();
            if (StringUtils.isNotBlank(ttl)) {
                return new OAuth20DeviceTokenExpirationPolicy(Beans.newDuration(ttl).getSeconds());
            }
        }
        return this.expirationPolicy.buildTicketExpirationPolicy();
    }
}
