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
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
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
            val decoded64 = EncodingUtils.decodeUrlSafeBase64(withoutPrefix);
            val decoded = (byte[]) cipherExecutor.decode(decoded64);
            val ticketContent = CompressionUtils.inflateToString(decoded);
            val ticketCompactor = findTicketCompactor(metadata);
            val ticketObject = ticketCompactor.expand(ticketContent);
            if (ticketObject != null && predicate.test(ticketObject)) {
                return ticketObject.markTicketStateless();
            }
            return null;
        });
    }

    @Override
    protected Ticket addSingleTicket(final Ticket ticket) throws Exception {
        val metadata = ticketCatalog.find(ticket.getPrefix());
        val ticketCompactor = findTicketCompactor(metadata);
        val compactedTicket = ticketCompactor.compact(ticket);
        val compressed = CompressionUtils.deflateToByteArray(compactedTicket);
        val encoded = (byte[]) cipherExecutor.encode(compressed);
        val encoded64 = EncodingUtils.encodeUrlSafeBase64(encoded);
        val finalTicketId = ticket.getPrefix() + UniqueTicketIdGenerator.SEPARATOR + encoded64;
        LOGGER.debug("Compacted ticket in encoded form is [{}]", finalTicketId);
        ticketCompactor.validate(finalTicketId);
        return new DefaultEncodedTicket(finalTicketId, ticket.getPrefix()).markTicketStateless();
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        return addSingleTicket(ticket);
    }

    protected TicketCompactor<Ticket> findTicketCompactor(final TicketDefinition metadata) {
        return ticketCompactors
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(compactor -> compactor.getTicketType().equals(metadata.getApiClass()))
            .min(AnnotationAwareOrderComparator.INSTANCE)
            .orElseThrow(() -> new IllegalStateException("No ticket compactor is registered to support " + metadata.getApiClass().getName()));
    }

}
