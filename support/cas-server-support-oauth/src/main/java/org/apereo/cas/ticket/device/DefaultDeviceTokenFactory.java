package org.apereo.cas.ticket.device;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

/**
 * Default OAuth device token factory.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultDeviceTokenFactory implements DeviceTokenFactory {
    private static final int DEVICE_CODE_LENGTH = 24;
    private static final int USER_CODE_LENGTH = 6;

    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator deviceTokenIdGenerator;

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicy expirationPolicy;

    public DefaultDeviceTokenFactory(final ExpirationPolicy expirationPolicy) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy);
    }

    @Override
    public DeviceToken create(final Service service) {
        val codeId = this.deviceTokenIdGenerator.getNewTicketId(DeviceToken.PREFIX);
        val deviceCode = RandomStringUtils.randomAlphanumeric(DEVICE_CODE_LENGTH);
        val userCode = RandomStringUtils.randomAlphanumeric(USER_CODE_LENGTH).toUpperCase();
        return new DeviceTokenImpl(codeId, service, deviceCode, userCode, expirationPolicy);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
