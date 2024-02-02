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
        builder.append(String.format("%s%s", DELIMITER, code.getService().getShortenedId()));
        builder.append(String.format("%s%s", DELIMITER, code.getUserCode()));
        return builder.toString();
    }

    @Override
    public Class<OAuth20DeviceToken> getTicketType() {
        return OAuth20DeviceToken.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val structure = parse(ticketId);
        val service = serviceFactory.createService(structure.ticketElements().get(CompactTicketIndexes.SERVICE.getIndex()));
        val userCode = structure.ticketElements().get(3);
        val codeFactory = (OAuth20DeviceTokenFactory) ticketFactory.getObject().get(getTicketType());
        val code = codeFactory.createDeviceCode(service);
        code.setUserCode(StringUtils.trimToNull(userCode));
        code.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        code.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return code;
    }

}
