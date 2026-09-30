package org.apereo.cas.ticket.device;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link OAuth20DeviceUserCodeCompactor}.
 * The user code is the ticket id, so it is kept in the compact ticket and retained on expansion.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class OAuth20DeviceUserCodeCompactor implements TicketCompactor<OAuth20DeviceUserCode> {
    private static final int APPROVED_INDEX = 3;

    private static final int USER_CODE_INDEX = 4;

    private final ServiceFactory serviceFactory;

    @Getter
    private long maximumTicketLength = 256;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
        val code = (OAuth20DeviceUserCode) ticket;
        fields.add(StringUtils.defaultString(code.getService().getShortenedId()));
        fields.add(BooleanUtils.toString(code.isUserCodeApproved(), "1", "0"));
        fields.add(ticket.getId());
    }

    @Override
    public Class<OAuth20DeviceUserCode> getTicketType() {
        return OAuth20DeviceUserCode.class;
    }

    @Override
    public boolean isTicketIdRetained() {
        return true;
    }

    @Override
    public Ticket expand(final String compactTicket) {
        val structure = parse(compactTicket, USER_CODE_INDEX + 1);
        val service = Objects.requireNonNull(serviceFactory.createService(structure.get(CompactTicketIndexes.SERVICE)));
        val userCode = new OAuth20DefaultDeviceUserCode(structure.get(USER_CODE_INDEX), service,
            new FixedInstantExpirationPolicy(structure.expirationTime()));
        userCode.setUserCodeApproved(BooleanUtils.toBoolean(structure.get(APPROVED_INDEX)));
        userCode.setTenantId(service.getTenant());
        userCode.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return userCode;
    }
}
