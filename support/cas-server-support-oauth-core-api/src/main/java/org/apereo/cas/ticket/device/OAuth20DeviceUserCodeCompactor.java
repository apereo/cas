package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link OAuth20DeviceUserCodeCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class OAuth20DeviceUserCodeCompactor implements TicketCompactor<OAuth20DeviceUserCode> {
    private final ObjectProvider<TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;

    @Getter
    private long maximumTicketLength = 256;

    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) throws Exception {
        val code = (OAuth20DeviceUserCode) ticket;
        builder.append(String.format("%s%s", DELIMITER, code.getService().getShortenedId()));
        val approved = BooleanUtils.toString(code.isUserCodeApproved(), "1", "0");
        builder.append(String.format("%s%s", DELIMITER, approved));
        builder.append(String.format("%s%s", DELIMITER, ticket.getId()));
        return builder.toString();
    }

    @Override
    public Class<OAuth20DeviceUserCode> getTicketType() {
        return OAuth20DeviceUserCode.class;
    }

    @Override
    public Ticket expand(final String ticketId) {
        val structure = parse(ticketId);
        val codeFactory = (OAuth20DeviceUserCodeFactory) ticketFactory.getObject().get(getTicketType());
        val service = serviceFactory.createService(structure.ticketElements().get(CompactTicketIndexes.SERVICE.getIndex()));
        val isApproved = BooleanUtils.toBoolean(structure.ticketElements().get(3));
        val id = structure.ticketElements().get(4);
        val userCode = codeFactory.createDeviceUserCode(id, service);
        userCode.setUserCodeApproved(isApproved);
        userCode.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        userCode.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return userCode;
    }
}
