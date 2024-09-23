package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;

import com.google.cloud.firestore.Filter;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is {@link GoogleCloudFirestoreTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class GoogleCloudFirestoreTicketRegistry extends AbstractTicketRegistry {
    private final Firestore firestore;

    public GoogleCloudFirestoreTicketRegistry(final CipherExecutor cipherExecutor, final TicketSerializationManager ticketSerializationManager,
                                              final TicketCatalog ticketCatalog, final ConfigurableApplicationContext applicationContext,
                                              final Firestore firestore) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.firestore = firestore;
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        return FunctionUtils.doUnchecked(() -> {
            LOGGER.debug("Locating ticket ticketId [{}]", ticketId);
            val encTicketId = digestIdentifier(ticketId);
            if (encTicketId == null) {
                LOGGER.debug("Ticket id [{}] could not be found", ticketId);
                return null;
            }
            val metadata = ticketCatalog.find(ticketId);
            if (metadata == null) {
                LOGGER.debug("Ticket definition [{}] could not be found in the ticket catalog", ticketId);
                return null;
            }
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            val documentSnapshot = firestore.collection(collectionName).document(encTicketId).get().get();
            if (documentSnapshot != null) {
                val document = documentSnapshot.toObject(GoogleCloudFirestoreTicketDocument.class);
                if (document != null) {
                    val decoded = deserializeTicket(document.getJson(), document.getType());
                    val result = decodeTicket(decoded);

                    if (predicate.test(result)) {
                        return result;
                    }
                }
            }
            return null;
        });
    }

    @Override
    public long deleteAll() {
        return ticketCatalog.findAll()
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .filter(StringUtils::isNotBlank)
            .map(firestore::collection)
            .mapToLong(collection -> FunctionUtils.doUnchecked(() -> {
                val count = collection.count().get().get().getCount();
                firestore.recursiveDelete(collection).get();
                return count;
            }))
            .sum();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return ticketCatalog.findAll().stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .flatMap(collectionName -> {
                val references = firestore.collection(collectionName).listDocuments();
                return StreamSupport.stream(references.spliterator(), false)
                    .map(doc -> FunctionUtils.doUnchecked(() -> doc.get().get()))
                    .map(doc -> doc.toObject(GoogleCloudFirestoreTicketDocument.class));
            })
            .map(doc -> {
                val ticket = deserializeTicket(doc.getJson(), doc.getType());
                return decodeTicket(ticket);
            })
            .filter(ticket -> !ticket.isExpired())
            .collect(Collectors.toSet());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        FunctionUtils.doAndHandle(__ -> {
            LOGGER.debug("Updating ticket [{}]", ticket.getId());
            val ticketDocument = buildTicketAsDocument(ticket);
            val metadata = ticketCatalog.find(ticket);
            LOGGER.trace("Located ticket definition [{}] in the ticket catalog", metadata);
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            LOGGER.trace("Found collection [{}] linked to ticket [{}]", collectionName, metadata);

            val writeResult = firestore.collection(collectionName)
                .document(ticketDocument.getTicketId())
                .update(ticketDocument.asUpdatableMap())
                .get();
            LOGGER.debug("Added ticket [{}] to [{}] @ [{}]", ticket.getId(), collectionName, writeResult.getUpdateTime());
        });
        return ticket;
    }

    @Override
    public long deleteSingleTicket(final Ticket ticketToDelete) {
        return FunctionUtils.doUnchecked(() -> {
            val ticketId = digestIdentifier(ticketToDelete.getId());
            LOGGER.debug("Deleting ticket [{}]", ticketId);
            val metadata = ticketCatalog.find(ticketToDelete);
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            val updateTime = firestore.collection(collectionName).document(ticketId).delete().get().getUpdateTime();
            LOGGER.debug("Deleted ticket [{}] from [{}] @ [{}]", ticketToDelete.getId(), collectionName, updateTime);
            return 1;
        });
    }

    @Override
    protected Ticket addSingleTicket(final Ticket ticket) {
        FunctionUtils.doAndHandle(__ -> {
            LOGGER.debug("Adding ticket [{}]", ticket.getId());
            val ticketDocument = buildTicketAsDocument(ticket);
            val metadata = ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.error("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
            } else {
                LOGGER.trace("Located ticket definition [{}] in the ticket catalog", metadata);
                val collectionName = getTicketCollectionInstanceByMetadata(metadata);
                LOGGER.trace("Found collection [{}] linked to ticket [{}]", collectionName, metadata);

                val writeResult = firestore.collection(collectionName)
                    .document(ticketDocument.getTicketId())
                    .create(ticketDocument)
                    .get();
                LOGGER.debug("Added ticket [{}] to [{}] @ [{}]", ticket.getId(), collectionName, writeResult.getUpdateTime());
            }
        });
        return ticket;
    }

    @Override
    public long serviceTicketCount() {
        return countTicketsByTicketType(ServiceTicket.class);
    }

    @Override
    public long sessionCount() {
        return countTicketsByTicketType(TicketGrantingTicket.class);
    }

    @Override
    public long countSessionsFor(final String principalId) {
        return getSessionsFor(principalId).count();
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val ticketDefinitions = ticketCatalog.findTicketImplementations(TicketGrantingTicket.class);
        return ticketDefinitions
            .stream()
            .flatMap(definition -> FunctionUtils.doUnchecked(() -> {
                val collection = getTicketCollectionInstanceByMetadata(definition);
                val spliterator = firestore.collection(collection).whereEqualTo("principal",
                    digestIdentifier(principalId)).get().get().spliterator();
                return StreamSupport.stream(spliterator, false);
            }))
            .map(document -> document.toObject(GoogleCloudFirestoreTicketDocument.class))
            .map(document -> deserializeTicket(document.getJson(), document.getType()))
            .map(this::decodeTicket)
            .filter(ticket -> !ticket.isExpired());
    }

    @Override
    public long countTicketsFor(final Service service) {
        return ticketCatalog
            .findAll()
            .stream()
            .map(Unchecked.function(definition -> {
                val collection = getTicketCollectionInstanceByMetadata(definition);
                val spliterator = firestore.collection(collection)
                    .whereEqualTo("service", service.getId())
                    .get()
                    .get()
                    .spliterator();
                return StreamSupport.stream(spliterator, false);
            }))
            .mapToLong(Stream::count)
            .sum();
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        val ticketDefinitions = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class);
        return ticketDefinitions
            .stream()
            .flatMap(Unchecked.function(definition -> {
                val collection = getTicketCollectionInstanceByMetadata(definition);
                val criterias = queryAttributes.entrySet()
                    .stream()
                    .map(entry -> entry.getValue()
                        .stream()
                        .map(queryValue -> {
                            val key = "attributes." + digestIdentifier(entry.getKey());
                            return Filter.arrayContains(key, digestIdentifier(queryValue.toString()));
                        })
                        .toList())
                    .flatMap(List::stream)
                    .toList();
                val spliterator = firestore.collection(collection)
                    .whereEqualTo("prefix", digestIdentifier(definition.getPrefix()))
                    .where(Filter.or(criterias.toArray(Filter[]::new)))
                    .get()
                    .get()
                    .spliterator();
                return StreamSupport.stream(spliterator, false);
            }))
            .map(document -> document.toObject(GoogleCloudFirestoreTicketDocument.class))
            .map(document -> deserializeTicket(document.getJson(), document.getType()))
            .map(this::decodeTicket)
            .filter(ticket -> !ticket.isExpired());
    }

    protected long countTicketsByTicketType(final Class<? extends Ticket> ticketType) {
        val ticketDefinitions = ticketCatalog.findTicketImplementations(ticketType);
        return ticketDefinitions
            .stream()
            .mapToLong(defn -> FunctionUtils.doUnchecked(() -> {
                val collection = getTicketCollectionInstanceByMetadata(defn);
                return firestore.collection(collection)
                    .whereEqualTo("prefix", digestIdentifier(defn.getPrefix()))
                    .count().get().get().getCount();
            }))
            .sum();
    }

    protected String getTicketCollectionInstanceByMetadata(final TicketDefinition metadata) {
        return metadata.getProperties().getStorageName();
    }

    protected GoogleCloudFirestoreTicketDocument buildTicketAsDocument(final Ticket ticket) throws Exception {
        val encTicket = encodeTicket(ticket);
        val json = serializeTicket(encTicket);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Serialized ticket into a JSON document as\n [{}]",
                JsonValue.readJSON(json).toString(Stringify.FORMATTED));
        }
        val principal = getPrincipalIdFrom(ticket);

        val expireAt = getExpireAt(ticket);
        LOGGER.trace("Calculated expiration date for ticket ttl as [{}]", expireAt);

        return GoogleCloudFirestoreTicketDocument.builder()
            .type(encTicket.getClass().getName())
            .prefix(digestIdentifier(ticket.getPrefix()))
            .ticketId(encTicket.getId())
            .json(json)
            .principal(digestIdentifier(principal))
            .attributes(collectAndDigestTicketAttributes(ticket))
            .expireAt(expireAt)
            .service(ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService()) ? sat.getService().getId() : StringUtils.EMPTY)
            .build();
    }

    protected Date getExpireAt(final Ticket ticket) {
        val expirationPolicy = ticket.getExpirationPolicy();
        val ttl = expirationPolicy.getTimeToLive(ticket);
        if (ttl < 1 || ttl == Long.MAX_VALUE) {
            LOGGER.trace("Expiration date is undefined for ttl value [{}]", ttl);
            return DateTimeUtils.dateOf(LocalDateTime.now(Clock.systemUTC()).plusYears(10));
        }
        val exp = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ttl);
        return DateTimeUtils.dateOf(Instant.ofEpochMilli(exp));
    }
}
