package org.apereo.cas.ticket.device;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.compact.TicketCompactor;
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
    private static final int USER_CODE_INDEX = 3;

    private static final int CLIENT_ID_INDEX = 4;

    private final ObjectProvider<TicketFactory> ticketFactory;

    private final ServiceFactory serviceFactory;

    @Getter
    private long maximumTicketLength = 256;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
        val code = (OAuth20DeviceToken) ticket;
        fields.add(StringUtils.defaultString(code.getService().getShortenedId()));
        fields.add(StringUtils.defaultString(code.getUserCode()));
        fields.add(StringUtils.defaultIfBlank(code.getClientId(), code.getService().getId()));
    }

    @Override
    public Class<OAuth20DeviceToken> getTicketType() {
        return OAuth20DeviceToken.class;
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, CLIENT_ID_INDEX + 1);
        val service = Objects.requireNonNull(serviceFactory.createService(structure.get(CompactTicketIndexes.SERVICE)));
        val factory = (OAuth20DeviceTokenFactory) ticketFactory.getObject().get(getTicketType());
        val code = factory.createDeviceCode(service, new ArrayList<>(), structure.get(CLIENT_ID_INDEX));
        code.setUserCode(StringUtils.trimToNull(structure.get(USER_CODE_INDEX)));
        code.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        code.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        return code;
    }
}
