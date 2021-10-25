package org.apereo.cas.util;

import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.TicketHolder;

import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

import java.time.Duration;
import java.util.ArrayList;

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

    private final boolean createIndexes;

    private final boolean dropExistingIndexes;
    
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

    private MongoCollection<Document> createTicketCollection(final TicketDefinition ticket) {
        val collectionName = ticket.getProperties().getStorageName();
        LOGGER.trace("Setting up MongoDb Ticket Registry instance [{}]", collectionName);
        MongoDbConnectionFactory.createCollection(mongoTemplate, collectionName, this.dropCollection);

        val collection = mongoTemplate.getCollection(collectionName);
        if (this.createIndexes) {
            if (this.dropExistingIndexes) {
                LOGGER.trace("Dropping existing indexes on collection [{}]...", collectionName);
                MongoDbConnectionFactory.dropCollectionIndexes(collection);
            }

            val columnsIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField(TicketHolder.FIELD_NAME_JSON)
                .onField(TicketHolder.FIELD_NAME_TYPE)
                .onField(TicketHolder.FIELD_NAME_ID)
                .build();
            val expireIndex = new Index().on(TicketHolder.FIELD_NAME_EXPIRE_AT, Sort.Direction.ASC);
            
            val timeout = ticket.getProperties().getStorageTimeout();
            if (timeout > 0 && timeout != Long.MAX_VALUE) {
                expireIndex.expire(Duration.ofSeconds(timeout));
            }

            val expectedIndexes = new ArrayList<IndexDefinition>();
            expectedIndexes.add(expireIndex);
            expectedIndexes.add(columnsIndex);
            LOGGER.debug("Expected indexes are [{}]", expectedIndexes);
            MongoDbConnectionFactory.createOrUpdateIndexes(mongoTemplate, collection, expectedIndexes);
        }
        return collection;
    }
}
