package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.ticket.RenewableServiceTicket;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    protected final CasConfigurationProperties casProperties;

    private final ObjectProvider<TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;

    private final Map<String, TicketCompactor> compactors = Map.of(
        TicketGrantingTicket.PREFIX, new TicketGrantingTicketCompactor(),
        ServiceTicket.PREFIX, new ServiceTicketCompactor(),
        TransientSessionTicket.PREFIX, new TransientSessionTicketCompactor());

    public StatelessTicketRegistry(final CipherExecutor<byte[], byte[]> cipherExecutor,
                                   final TicketSerializationManager ticketSerializationManager,
                                   final TicketCatalog ticketCatalog,
                                   final ObjectProvider<TicketFactory> ticketFactory,
                                   final ServiceFactory serviceFactory,
                                   final CasConfigurationProperties casProperties) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog);
        this.casProperties = casProperties;
        this.ticketFactory = ticketFactory;
        this.serviceFactory = serviceFactory;
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        return FunctionUtils.doAndHandle(() -> {
            val metadata = ticketCatalog.find(ticketId);
            val withoutPrefix = StringUtils.removeStart(ticketId, metadata.getPrefix() + UniqueTicketIdGenerator.SEPARATOR);
            val decoded64 = EncodingUtils.decodeBase64(withoutPrefix);
            val decoded = (byte[]) cipherExecutor.decode(decoded64);
            val ticketContent = CompressionUtils.inflateToString(decoded);
            val ticketObject = compactors.get(metadata.getPrefix()).expand(ticketContent);
            if (ticketObject != null && predicate.test(ticketObject)) {
                ticketObject.markTicketCompact();
                return ticketObject;
            }
            return null;
        });
    }

    @Override
    protected Ticket addSingleTicket(final Ticket ticket) throws Exception {
        val compactedTicket = compactors.get(ticket.getPrefix()).compact(ticket);
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

    @FunctionalInterface
    private interface TicketCompactor {
        /**
         * Expand ticket.
         *
         * @param ticketId the ticket id
         * @return the ticket
         * @throws Throwable the throwable
         */
        Ticket expand(String ticketId) throws Throwable;

        /**
         * Compact ticket.
         *
         * @param ticket the ticket
         * @return the string
         * @throws Exception the exception
         */
        default String compact(final Ticket ticket) throws Exception {
            val creationTime = ticket.getCreationTime().toEpochSecond();
            val expirationTime = ticket.getExpirationPolicy().toMaximumExpirationTime(ticket).toEpochSecond();
            val builder = new StringBuilder(String.format("%s,%s", creationTime, expirationTime));
            return compact(builder, ticket);
        }

        /**
         * Compact string from a builder.
         *
         * @param compactBuilder the compact builder
         * @param ticket         the ticket
         * @return the string
         * @throws Exception the exception
         */
        @SuppressWarnings("UnusedVariable")
        default String compact(final StringBuilder compactBuilder, final Ticket ticket) throws Exception {
            return compactBuilder.toString();
        }
    }

    private final class TicketGrantingTicketCompactor implements TicketCompactor {
        @Override
        public String compact(final Ticket ticket) throws Exception {
            return ticketSerializationManager.serializeTicket(ticket);
        }

        @Override
        public Ticket expand(final String ticketId) {
            return ticketSerializationManager.deserializeTicket(ticketId, TicketGrantingTicket.class);
        }
    }

    private final class TransientSessionTicketCompactor implements TicketCompactor {
        @Override
        public String compact(final StringBuilder builder, final Ticket ticket) {
            if (ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService())) {
                builder.append(String.format(",%s", StringUtils.defaultString(sat.getService().getId())));
            } else {
                builder.append(",*");
            }
            return builder.toString();
        }

        @Override
        public Ticket expand(final String ticketId) throws Throwable {
            val transientSessionTicketFactory = (TransientSessionTicketFactory) ticketFactory.getObject().get(TransientSessionTicket.class);
            val ticketElements = List.of(org.springframework.util.StringUtils.commaDelimitedListToStringArray(ticketId));

            val creationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(0)));
            val expirationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(1)));
            val service = serviceFactory.createService(ticketElements.get(2));

            val transientTicket = transientSessionTicketFactory.create(service, Map.of());
            transientTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(expirationTimeInSeconds));
            transientTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(creationTimeInSeconds));
            return transientTicket;
        }
    }
    
    private final class ServiceTicketCompactor implements TicketCompactor {
        @Override
        public String compact(final StringBuilder builder, final Ticket ticket) {
            if (ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService())) {
                builder.append(String.format(",%s", StringUtils.defaultString(sat.getService().getId())));
            } else {
                builder.append(",*");
            }
            if (ticket instanceof final RenewableServiceTicket rst) {
                builder.append(String.format(",%s", BooleanUtils.toString(rst.isFromNewLogin(), "1", "0")));
            } else {
                builder.append(",0");
            }
            return builder.toString();
        }

        @Override
        public Ticket expand(final String ticketId) throws Throwable {
            val serviceTicketFactory = (ServiceTicketFactory) ticketFactory.getObject().get(ServiceTicket.class);
            val ticketElements = List.of(org.springframework.util.StringUtils.commaDelimitedListToStringArray(ticketId));
            val creationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(0)));
            val expirationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(1)));
            val service = serviceFactory.createService(ticketElements.get(2));
            val credentialsProvided = BooleanUtils.toBoolean(ticketElements.get(3));
            val serviceTicket = serviceTicketFactory.create(service, credentialsProvided, ServiceTicket.class);
            serviceTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(expirationTimeInSeconds));
            serviceTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(creationTimeInSeconds));
            return serviceTicket;
        }
    }
}
