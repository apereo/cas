package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.compact.TicketCompactor;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * This is {@link StatelessTicketRegistry}.
 * A ticket id is the ticket prefix followed by the encrypted, base64url-encoded compact ticket.
 * The encrypted content starts with one byte that says whether the compact ticket is deflated,
 * followed by the length and bytes of the ticket prefix it was issued under. The prefix is checked
 * on every read, so a ticket presented under another prefix is rejected instead of being
 * expanded as a different ticket type.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Monitorable
@Slf4j
public class StatelessTicketRegistry extends AbstractTicketRegistry {
    private static final byte UNCOMPRESSED = 0;

    private static final byte DEFLATED = 1;

    private static final int BUFFER_SIZE = 1024;

    private static final int HEADER_LENGTH = 2;

    private static final int MAX_PREFIX_LENGTH = 255;

    private final List<TicketCompactor<? extends Ticket>> ticketCompactors;

    private final Map<Class<?>, TicketCompactor<? extends Ticket>> ticketCompactorsByType;

    public StatelessTicketRegistry(final CipherExecutor<byte[], byte[]> cipherExecutor,
                                   final TicketSerializationManager ticketSerializationManager,
                                   final TicketCatalog ticketCatalog,
                                   final ConfigurableApplicationContext applicationContext,
                                   final List<TicketCompactor<? extends Ticket>> compactors) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.ticketCompactors = List.copyOf(compactors);
        val compactorsByType = new HashMap<Class<?>, TicketCompactor<? extends Ticket>>();
        ticketCompactors
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(compactor -> compactorsByType.putIfAbsent(compactor.getTicketType(), compactor));
        this.ticketCompactorsByType = Map.copyOf(compactorsByType);
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        return FunctionUtils.doAndHandle(() -> {
            val metadata = ticketCatalog.find(ticketId);
            val ticketCompactor = findTicketCompactor(metadata);
            val withoutPrefix = Strings.CI.removeStart(ticketId, metadata.getPrefix() + UniqueTicketIdGenerator.SEPARATOR);
            val decrypted = (byte[]) Objects.requireNonNull(cipherExecutor.decode(EncodingUtils.decodeUrlSafeBase64(withoutPrefix)));
            val compactTicket = decompress(decrypted, metadata.getPrefix());
            LOGGER.trace("Raw compacted ticket to expand is [{}]", compactTicket);
            val ticketObject = ticketCompactor.expand(compactTicket);
            if (ticketObject instanceof final AbstractTicket expandedTicket && !ticketCompactor.isTicketIdRetained()) {
                expandedTicket.setId(ticketId);
            }
            if (ticketObject != null && predicate.test(ticketObject)) {
                ticketObject.markTicketStateless();
                return ticketObject;
            }
            return null;
        });
    }

    @Override
    protected Ticket addSingleTicket(final Ticket ticket) throws Exception {
        return compactTicket(ticket);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        return compactTicket(ticket);
    }

    protected Ticket compactTicket(final Ticket ticket) throws Exception {
        val metadata = ticketCatalog.find(ticket.getPrefix());
        val ticketCompactor = findTicketCompactor(metadata);
        val compactedTicket = ticketCompactor.compact(ticket);
        LOGGER.trace("Raw compacted ticket to add is [{}]", compactedTicket);
        val encrypted = (byte[]) Objects.requireNonNull(cipherExecutor.encode(compress(metadata.getPrefix(), compactedTicket)));
        val finalTicketId = ticket.getPrefix() + UniqueTicketIdGenerator.SEPARATOR + EncodingUtils.encodeUrlSafeBase64(encrypted);
        LOGGER.debug("Compacted ticket in encoded form is [{}]", finalTicketId);

        val encodedToken = new DefaultEncodedTicket(finalTicketId, ticket.getPrefix());
        encodedToken.markTicketStateless();
        val expirationTime = ticket.getExpirationPolicy().toMaximumExpirationTime(ticket).toEpochSecond();
        encodedToken.setExpirationPolicy(new FixedInstantExpirationPolicy(Instant.ofEpochSecond(expirationTime)));
        encodedToken.setCreationTime(ZonedDateTime.now(Clock.systemUTC()));
        return ticketCompactor.validate(encodedToken);
    }

    protected TicketCompactor<? extends Ticket> findTicketCompactor(final TicketDefinition metadata) {
        val compactor = ticketCompactorsByType.get(metadata.getApiClass());
        if (compactor == null) {
            throw new IllegalStateException("No ticket compactor is registered to support " + metadata.getApiClass().getName());
        }
        return compactor;
    }

    private static byte[] compress(final String prefix, final String compactTicket) {
        val prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
        if (prefixBytes.length > MAX_PREFIX_LENGTH) {
            throw new IllegalArgumentException("Ticket prefix %s is too long".formatted(prefix));
        }
        val content = compactTicket.getBytes(StandardCharsets.UTF_8);
        val deflated = deflate(content);
        val useDeflated = deflated.length < content.length;
        val body = useDeflated ? deflated : content;
        val bodyStart = HEADER_LENGTH + prefixBytes.length;
        val result = new byte[bodyStart + body.length];
        result[0] = useDeflated ? DEFLATED : UNCOMPRESSED;
        result[1] = (byte) prefixBytes.length;
        System.arraycopy(prefixBytes, 0, result, HEADER_LENGTH, prefixBytes.length);
        System.arraycopy(body, 0, result, bodyStart, body.length);
        return result;
    }

    private static String decompress(final byte[] content, final String expectedPrefix) throws DataFormatException {
        if (content.length < HEADER_LENGTH) {
            throw new DataFormatException("Compact ticket is empty");
        }
        val bodyStart = HEADER_LENGTH + Byte.toUnsignedInt(content[1]);
        if (content.length < bodyStart) {
            throw new DataFormatException("Compact ticket is truncated");
        }
        val prefix = new String(content, HEADER_LENGTH, bodyStart - HEADER_LENGTH, StandardCharsets.UTF_8);
        if (!prefix.equals(expectedPrefix)) {
            throw new DataFormatException("Compact ticket was issued as %s, not %s".formatted(prefix, expectedPrefix));
        }
        return switch (content[0]) {
            case UNCOMPRESSED -> new String(content, bodyStart, content.length - bodyStart, StandardCharsets.UTF_8);
            case DEFLATED -> new String(inflate(content, bodyStart), StandardCharsets.UTF_8);
            default -> throw new DataFormatException("Unknown compact ticket encoding " + content[0]);
        };
    }

    private static byte[] deflate(final byte[] content) {
        try (val deflater = new Deflater(Deflater.BEST_COMPRESSION, true)) {
            deflater.setInput(content);
            deflater.finish();
            val output = new ByteArrayOutputStream(content.length);
            val buffer = new byte[BUFFER_SIZE];
            while (!deflater.finished()) {
                output.write(buffer, 0, deflater.deflate(buffer));
            }
            return output.toByteArray();
        }
    }

    private static byte[] inflate(final byte[] content, final int offset) throws DataFormatException {
        try (val inflater = new Inflater(true)) {
            val input = Arrays.copyOfRange(content, offset, content.length + 1);
            inflater.setInput(input);
            val output = new ByteArrayOutputStream((content.length - offset) * 4);
            val buffer = new byte[BUFFER_SIZE];
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer);
                if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                    throw new DataFormatException("Compact ticket is truncated");
                }
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }
}
