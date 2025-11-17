package org.apereo.cas.ticket.registry.compact;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * This is {@link TransientSessionTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class TransientSessionTicketCompactor implements TicketCompactor<TransientSessionTicket> {
    private final ObjectProvider<@NonNull TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;

    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) throws Exception {
        val transientTicket = (TransientSessionTicket) ticket;
        builder.append(String.format("%s%s", DELIMITER, transientTicket.getService() != null
            ? transientTicket.getService().getShortenedId() : StringUtils.EMPTY));
        
        val properties = transientTicket.getProperties()
            .entrySet()
            .stream()
            .map(entry -> {
                val values = CollectionUtils.toCollection(entry.getValue())
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(";"));
                return entry.getKey() + '=' + values;
            })
            .reduce((s1, s2) -> s1 + '|' + s2)
            .orElse(StringUtils.EMPTY);
        builder.append(String.format("%s%s", DELIMITER, properties));
        return builder.toString();
    }

    @Override
    public Class<TransientSessionTicket> getTicketType() {
        return TransientSessionTicket.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val structure = parse(ticketId);
        val transientSessionTicketFactory = (TransientSessionTicketFactory) ticketFactory.getObject().get(getTicketType());
        val url = structure.ticketElements().get(CompactTicketIndexes.SERVICE.getIndex());
        val service = StringUtils.isNotBlank(url) ? serviceFactory.createService(url) : null;
        val properties = new HashMap<>();
        val compressProperties = structure.ticketElements().get(3);
        val keyValueProps = Splitter.on("|").splitToList(compressProperties);
        for (val keyValue : keyValueProps) {
            val key = StringUtils.substringBefore(keyValue, "=");
            val values = Splitter.on(";").splitToList(StringUtils.substringAfter(keyValue, "="));
            if (!values.isEmpty()) {
                properties.put(key, values.size() == 1 ? values.getFirst() : values);
            }
        }
        val transientTicket = transientSessionTicketFactory.create(service, properties);
        transientTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        transientTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return transientTicket;
    }
}
