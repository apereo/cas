package org.apereo.cas.ticket.device;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.RandomUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Default OAuth device token factory.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class OAuth20DefaultDeviceUserCodeFactory implements OAuth20DeviceUserCodeFactory {
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

    @Override
    public OAuth20DeviceUserCode createDeviceUserCode(final OAuth20DeviceToken deviceToken) {
        val userCode = generateDeviceUserCode(RandomUtils.randomAlphanumeric(userCodeLength));
        val expirationPolicyToUse = OAuth20DeviceTokenUtils.determineExpirationPolicyForService(servicesManager,
            expirationPolicy, deviceToken.getService());
        val deviceUserCode = new OAuth20DefaultDeviceUserCode(userCode, deviceToken.getId(), expirationPolicyToUse);
        deviceToken.assignUserCode(deviceUserCode);
        return deviceUserCode;
    }

    @Override
    public String generateDeviceUserCode(final String providedCode) {
        val prefix = OAuth20DeviceUserCode.PREFIX + '-';
        if (providedCode.startsWith(prefix)) {
            return providedCode;
        }
        return prefix + providedCode.toUpperCase();
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OAuth20DeviceUserCode.class;
    }

}
