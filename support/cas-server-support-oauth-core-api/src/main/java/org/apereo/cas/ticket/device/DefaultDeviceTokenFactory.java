package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;

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
    protected final ExpirationPolicy expirationPolicy;

    /**
     * Length of the generated user code.
     */
    protected final int userCodeLength;

    public DefaultDeviceTokenFactory(final ExpirationPolicy expirationPolicy) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy, USER_CODE_LENGTH);
    }

    @Override
    public DeviceToken createDeviceCode(final Service service) {
        val codeId = deviceTokenIdGenerator.getNewTicketId(DeviceToken.PREFIX);
        return new DeviceTokenImpl(codeId, service, expirationPolicy);
    }

    @Override
    public DeviceUserCode createDeviceUserCode(final DeviceToken deviceToken) {
        val userCode = generateDeviceUserCode(RandomStringUtils.randomAlphanumeric(userCodeLength));
        val deviceUserCode = new DeviceUserCodeImpl(userCode, deviceToken.getId(), this.expirationPolicy);
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
}
