package org.apereo.cas.ticket;

import org.apereo.cas.ticket.registry.AbstractTicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is {@link CosmosDbTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class CosmosDbTicketRegistry extends AbstractTicketRegistry {
    /**
     * Partition key.
     */
    public static final String PARTITION_KEY_PREFIX = "prefix";

    private final List<CosmosContainer> cosmosContainers;

    public CosmosDbTicketRegistry(final CipherExecutor cipherExecutor,
                                  final TicketSerializationManager ticketSerializationManager,
                                  final TicketCatalog ticketCatalog,
                                  final ConfigurableApplicationContext applicationContext,
                                  final List<CosmosContainer> cosmosContainers) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.cosmosContainers = cosmosContainers;
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        try {
            val encTicketId = digestIdentifier(ticketId);
            val metadata = StringUtils.isNotBlank(ticketId) ? ticketCatalog.find(ticketId) : null;
            if (metadata == null || StringUtils.isBlank(encTicketId)) {
                return null;
            }
            val container = getTicketContainer(metadata);
            LOGGER.debug("Reading ticket with id [{}] from [{}]", encTicketId, container.getId());
            val document = container.readItem(encTicketId, new PartitionKey(metadata.getPrefix()), CosmosDbTicketDocument.class).getItem();
            val result = decodeTicket(ticketSerializationManager.deserializeTicket(document.getTicket(), document.getType()));
            return predicate != null && predicate.test(result) ? result : null;
        } catch (final NotFoundException e) {
            LOGGER.debug("Ticket id [{}] cannot be found", ticketId);
            return null;
        } 
    }

    @Override
    public long deleteAll() {
        val queryOptions = new CosmosQueryRequestOptions();
        return ticketCatalog.findAll()
            .stream()
            .map(defn -> Pair.of(defn, getTicketContainer(defn)))
            .mapToLong(pair -> {
                val container = pair.getValue();
                val items = container.queryItems("SELECT * FROM " + container.getId(), queryOptions, CosmosDbTicketDocument.class);
                val queries = StreamSupport.stream(items.iterableByPage().spliterator(), false)
                    .map(response -> response.getResults()
                        .stream()
                        .map(doc -> CosmosBulkOperations.getDeleteItemOperation(doc.getId(), new PartitionKey(pair.getKey().getPrefix())))
                        .collect(Collectors.toList()))
                    .flatMap(List::stream)
                    .toList();
                return Iterables.size(pair.getValue().executeBulkOperations(queries));
            })
            .sum();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        val readOps = ticketCatalog.findAll()
            .stream()
            .map(defn -> {
                val container = getTicketContainer(defn);
                return CompletableFuture.supplyAsync(() -> {
                    LOGGER.trace("Reading tickets in container [{}]", defn.getPrefix());
                    return container
                        .readAllItems(new PartitionKey(defn.getPrefix()), CosmosDbTicketDocument.class)
                        .stream()
                        .toList();
                });
            }).toList();
        val allFutures = CompletableFuture.allOf(readOps.toArray(new CompletableFuture[0]));
        val allCompletableFuture = allFutures.thenApply(future -> readOps.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()))
            .thenApply(list -> list
                .stream()
                .flatMap(List::stream)
                .map(doc -> decodeTicket(ticketSerializationManager.deserializeTicket(doc.getTicket(), doc.getType())))
                .collect(Collectors.toList()));
        return FunctionUtils.doUnchecked(allCompletableFuture::get);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        return addTicket(ticket);
    }

    @Override
    public long deleteSingleTicket(final Ticket ticketToDelete) {
        val encTicketId = digestIdentifier(ticketToDelete.getId());
        val metadata = ticketCatalog.find(ticketToDelete);
        val container = getTicketContainer(metadata);
        val result = container.deleteItem(encTicketId, new PartitionKey(metadata.getPrefix()), new CosmosItemRequestOptions());
        return HttpStatus.valueOf(result.getStatusCode()).is2xxSuccessful() ? 1 : 0;
    }

    @Override
    protected Ticket addSingleTicket(final Ticket ticket) throws Exception {
        val metadata = ticketCatalog.find(ticket);
        val container = getTicketContainer(metadata);
        val document = getCosmosDbTicketDocument(ticket, metadata);
        container.upsertItem(document);
        return ticket;
    }

    @Override
    public List<? extends Ticket> addTicket(final Stream<? extends Ticket> toSave) {
        val operations = new HashMap<String, List<CosmosItemOperation>>();
        val results = toSave.peek(ticket -> {
            val ticketDefinition = ticketCatalog.find(ticket);
            val document = getCosmosDbTicketDocument(ticket, ticketDefinition);
            val commands = operations.getOrDefault(ticketDefinition.getProperties().getStorageName(), new ArrayList<>());
            commands.add(CosmosBulkOperations.getCreateItemOperation(document, new PartitionKey(ticketDefinition.getPrefix())));
            operations.put(ticketDefinition.getProperties().getStorageName(), commands);
        }).toList();
        operations.forEach((key, value) -> {
            val container = getTicketContainer(key);
            val result = container.executeBulkOperations(value);
            result.forEach(r -> {
                if (r.getResponse().getStatusCode() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                    container.executeBulkOperations(List.of(r.getOperation()));
                }
            });
        });
        return results;
    }

    private CosmosDbTicketDocument getCosmosDbTicketDocument(final Ticket ticket, final TicketDefinition metadata) {
        val encodedTicket = FunctionUtils.doUnchecked(() -> encodeTicket(ticket));
        val ttl = ticket.getExpirationPolicy().getTimeToLive();
        return CosmosDbTicketDocument
            .builder()
            .id(encodedTicket.getId())
            .type(encodedTicket.getClass().getName())
            .principal(digestIdentifier(getPrincipalIdFrom(ticket)))
            .timeToLive(ttl)
            .ticket(ticketSerializationManager.serializeTicket(encodedTicket))
            .prefix(metadata.getPrefix())
            .build();
    }

    private CosmosContainer getTicketContainer(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating container [{}] for ticket definition [{}]", mapName, metadata);
        return getTicketContainer(mapName);
    }

    private CosmosContainer getTicketContainer(final String containerId) {
        return cosmosContainers
            .stream()
            .filter(cosmosContainer -> cosmosContainer.getId().equalsIgnoreCase(containerId))
            .findFirst()
            .orElseThrow();
    }
}
