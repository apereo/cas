package org.apereo.cas.ticket.registry;

import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import java.util.function.Predicate;

/**
 * This is {@link StatelessTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Monitorable
@Slf4j
public class StatelessTicketRegistry extends AbstractTicketRegistry {
    private final List<TicketCompactor<Ticket>> ticketCompactors;

    public StatelessTicketRegistry(final CipherExecutor<byte[], byte[]> cipherExecutor,
                                   final TicketSerializationManager ticketSerializationManager,
                                   final TicketCatalog ticketCatalog,
                                   final List<TicketCompactor<Ticket>> compactors) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog);
        this.ticketCompactors = List.copyOf(compactors);
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        return FunctionUtils.doAndHandle(() -> {
            val metadata = ticketCatalog.find(ticketId);
            val withoutPrefix = StringUtils.removeStart(ticketId, metadata.getPrefix() + UniqueTicketIdGenerator.SEPARATOR);
            val decoded64 = EncodingUtils.decodeBase64(withoutPrefix);
            val decoded = (byte[]) cipherExecutor.decode(decoded64);
            val ticketContent = CompressionUtils.inflateToString(decoded);
            val ticketObject = findTicketCompactor(metadata).expand(ticketContent);
            if (ticketObject != null && predicate.test(ticketObject)) {
                ticketObject.markTicketCompact();
                return ticketObject;
            }
            return null;
        });
    }

    @Override
    protected Ticket addSingleTicket(final Ticket ticket) throws Exception {
        val metadata = ticketCatalog.find(ticket.getPrefix());
        val compactedTicket = findTicketCompactor(metadata).compact(ticket);
        val compressed = CompressionUtils.deflateToByteArray(compactedTicket);
        val encoded = (byte[]) cipherExecutor.encode(compressed);
        val encoded64 = EncodingUtils.encodeBase64(encoded);
        val finalTicketId = ticket.getPrefix() + UniqueTicketIdGenerator.SEPARATOR + encoded64;
        LOGGER.debug("Compacted ticket in encoded form is [{}]", finalTicketId);
        return new DefaultEncodedTicket(finalTicketId, ticket.getPrefix()).markTicketCompact();
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        return addSingleTicket(ticket);
    }

    protected TicketCompactor<Ticket> findTicketCompactor(final TicketDefinition metadata) {
        return ticketCompactors
            .stream()
            .filter(compactor -> compactor.getTicketType().equals(metadata.getApiClass()))
            .findFirst()
            .orElseThrow();
    }

}
