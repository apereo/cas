package org.apereo.cas.ticket.device;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link OAuth20DeviceTokenCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class OAuth20DeviceTokenCompactor implements TicketCompactor<OAuth20DeviceToken> {
    private final ObjectProvider<TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;

    @Getter
    private long maximumTicketLength = 256;

    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) throws Exception {
        val code = (OAuth20DeviceToken) ticket;
        builder.append(DELIMITER).append(TicketCompactor.encodeValue(code.getService().getShortenedId()));
        builder.append(DELIMITER).append(TicketCompactor.encodeValue(code.getUserCode()));
        builder.append(DELIMITER).append(TicketCompactor.encodeValue(StringUtils.defaultIfBlank(code.getClientId(), code.getService().getId())));
        return builder.toString();
    }

    @Override
    public Class<OAuth20DeviceToken> getTicketType() {
        return OAuth20DeviceToken.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val structure = parse(ticketId, 5);
        val service = serviceFactory.createService(TicketCompactor.decodeValue(structure.ticketElements().get(CompactTicketIndexes.SERVICE.getIndex())));
        val userCode = TicketCompactor.decodeValue(structure.ticketElements().get(3));
        val clientId = TicketCompactor.decodeValue(structure.ticketElements().get(4));
        val codeFactory = (OAuth20DeviceTokenFactory) ticketFactory.getObject().get(getTicketType());
        val code = codeFactory.createDeviceCode(service, new ArrayList<>(), clientId);
        code.setUserCode(StringUtils.trimToNull(userCode));
        code.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        code.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return code;
    }

}
