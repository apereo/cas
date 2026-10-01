package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link TransientSessionTicketCompactor}.
 * Properties are kept as strings; a single value is restored as a string, several values as a list.
 * The service is kept in full rather than shortened, since flows resume with it, such as delegated authentication
 * returning to the SAML2 identity provider callback with its query parameters.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class TransientSessionTicketCompactor implements TicketCompactor<TransientSessionTicket> {
    private static final int PROPERTIES_INDEX = 3;

    private final ObjectProvider<TicketFactory> ticketFactory;

    private final ServiceFactory serviceFactory;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
        val transientTicket = (TransientSessionTicket) ticket;
        val service = transientTicket.getService();
        fields.add(service != null ? StringUtils.defaultString(service.getId()) : StringUtils.EMPTY);
        val properties = new ArrayList<String>();
        transientTicket.getProperties().forEach((key, value) -> {
            properties.add(key);
            properties.add(CompactTicketCodec.encodeValues(CollectionUtils.toCollection(value)));
        });
        fields.add(CompactTicketCodec.encodeValues(properties));
    }

    @Override
    public Class<TransientSessionTicket> getTicketType() {
        return TransientSessionTicket.class;
    }

    @Override
    @SuppressWarnings("NullAway")
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, PROPERTIES_INDEX + 1);
        val url = structure.get(CompactTicketIndexes.SERVICE);
        val service = StringUtils.isNotBlank(url) ? serviceFactory.createService(url) : null;
        val properties = new HashMap<String, Serializable>();
        val entries = CompactTicketCodec.decodeValues(structure.get(PROPERTIES_INDEX));
        if (entries.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid transient ticket properties");
        }
        for (var index = 0; index < entries.size(); index += 2) {
            val values = CompactTicketCodec.decodeValues(entries.get(index + 1));
            if (!values.isEmpty()) {
                properties.put(entries.get(index), values.size() == 1 ? values.getFirst() : new ArrayList<>(values));
            }
        }
        val factory = (TransientSessionTicketFactory<?>) ticketFactory.getObject().get(getTicketType());
        val transientTicket = factory.create(service, properties);
        transientTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        transientTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        return transientTicket;
    }
}
