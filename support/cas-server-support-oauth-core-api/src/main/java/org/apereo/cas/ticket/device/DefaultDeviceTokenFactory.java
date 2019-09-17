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
public class DefaultDeviceTokenFactory implements DeviceTokenFactory {
    private static final int USER_CODE_LENGTH = 8;

    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator deviceTokenIdGenerator;

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicyBuilder<DeviceToken> expirationPolicy;

    /**
     * Length of the generated user code.
     */
    protected final int userCodeLength;


    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;


    public DefaultDeviceTokenFactory(final ExpirationPolicyBuilder<DeviceToken> expirationPolicy,
                                     final ServicesManager servicesManager) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy, USER_CODE_LENGTH, servicesManager);
    }

    @Override
    public DeviceToken createDeviceCode(final Service service) {
        val codeId = deviceTokenIdGenerator.getNewTicketId(DeviceToken.PREFIX);
        val expirationPolicyToUse = determineExpirationPolicyForService(service);
        return new DeviceTokenImpl(codeId, service, expirationPolicyToUse);
    }

    @Override
    public DeviceUserCode createDeviceUserCode(final DeviceToken deviceToken) {
        val userCode = generateDeviceUserCode(RandomUtils.randomAlphanumeric(userCodeLength));
        val expirationPolicyToUse = determineExpirationPolicyForService(deviceToken.getService());
        val deviceUserCode = new DeviceUserCodeImpl(userCode, deviceToken.getId(), expirationPolicyToUse);
        deviceToken.assignUserCode(deviceUserCode);
        return deviceUserCode;
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }

    @Override
    public String generateDeviceUserCode(final String providedCode) {
        val prefix = DeviceUserCode.PREFIX + '-';
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
                return new DeviceTokenExpirationPolicy(Beans.newDuration(ttl).getSeconds());
            }
        }
        return this.expirationPolicy.buildTicketExpirationPolicy();
    }
}
