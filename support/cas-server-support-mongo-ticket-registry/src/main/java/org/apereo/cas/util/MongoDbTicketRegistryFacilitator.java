package org.apereo.cas.util;

import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.TicketHolder;

import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

import java.time.Duration;
import java.util.List;

/**
 * This is {@link MongoDbTicketRegistryFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class MongoDbTicketRegistryFacilitator {
    private final TicketCatalog ticketCatalog;

    private final MongoTemplate mongoTemplate;

    private final boolean dropCollection;

    /**
     * Create ticket collections.
     */
    public void createTicketCollections() {
        val definitions = ticketCatalog.findAll();
        definitions.forEach(t -> {
            val c = createTicketCollection(t);
            LOGGER.debug("Created MongoDb collection configuration for [{}]", c.getNamespace().getFullName());
        });
        LOGGER.info("Configured MongoDb Ticket Registry instance with available collections: [{}]", mongoTemplate.getCollectionNames());
    }

    private MongoCollection createTicketCollection(final TicketDefinition ticket) {
        val collectionName = ticket.getProperties().getStorageName();
        LOGGER.trace("Setting up MongoDb Ticket Registry instance [{}]", collectionName);
        MongoDbConnectionFactory.createCollection(mongoTemplate, collectionName, this.dropCollection);

        LOGGER.trace("Creating indexes on collection [{}]...", collectionName);
        val collection = mongoTemplate.getCollection(collectionName);
        val expireIndex = new Index().on(TicketHolder.FIELD_NAME_EXPIRE_AT, Sort.Direction.ASC)
            .expire(Duration.ofSeconds(ticket.getProperties().getStorageTimeout()));
        val columnsIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
            .onField(TicketHolder.FIELD_NAME_JSON)
            .onField(TicketHolder.FIELD_NAME_TYPE)
            .onField(TicketHolder.FIELD_NAME_ID)
            .build();
        MongoDbConnectionFactory.createOrUpdateIndexes(mongoTemplate, collection, List.of(expireIndex, columnsIndex));
        return collection;
    }
}
