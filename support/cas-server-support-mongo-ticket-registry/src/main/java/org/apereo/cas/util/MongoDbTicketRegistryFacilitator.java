package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.configuration.model.support.mongo.ticketregistry.MongoDbTicketRegistryProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.MongoDbTicketDocument;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;

/**
 * This is {@link MongoDbTicketRegistryFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class MongoDbTicketRegistryFacilitator {
    /**
     * Index name for ticket id.
     * When/if changing index names, be sure to also update the field
     * documentation that lists all supported indexes.
     */
    private static final String INDEX_NAME_ID = "IDX_ID";
    private static final String INDEX_NAME_PRINCIPAL = "IDX_PRINCIPAL";
    private static final String INDEX_NAME_EXPIRATION = "IDX_EXPIRATION";

    private final TicketCatalog ticketCatalog;

    private final MongoOperations mongoTemplate;

    private final MongoDbTicketRegistryProperties properties;
    
    /**
     * Create ticket collections.
     */
    public void createTicketCollections() {
        val definitions = ticketCatalog.findAll();
        definitions.forEach(t -> {
            val collection = createTicketCollection(t);
            LOGGER.debug("Created MongoDb collection configuration for [{}]", collection.getNamespace().getFullName());
        });
        LOGGER.info("Configured MongoDb Ticket Registry instance with available collections: [{}]", mongoTemplate.getCollectionNames());
    }

    private MongoCollection<Document> createTicketCollection(final TicketDefinition ticket) {
        val collectionName = ticket.getProperties().getStorageName();
        LOGGER.trace("Setting up MongoDb Ticket Registry instance [{}]", collectionName);
        MongoDbConnectionFactory.createCollection(mongoTemplate, collectionName, properties.isDropCollection());

        val collection = mongoTemplate.getCollection(collectionName);
        if (properties.isUpdateIndexes()) {
            if (properties.isDropIndexes()) {
                LOGGER.trace("Dropping existing indexes on collection [{}]...", collectionName);
                MongoDbConnectionFactory.dropCollectionIndexes(collection);
            }
            val expectedIndexes = new ArrayList<IndexDefinition>();

            if (properties.getIndexes().isEmpty() || properties.getIndexes().contains(INDEX_NAME_ID)) {
                val ticketIdIndex = new Index()
                    .named(INDEX_NAME_ID)
                    .on(MongoDbTicketDocument.FIELD_NAME_ID, Sort.Direction.ASC);
                expectedIndexes.add(ticketIdIndex);
            }

            if (ticket.getApiClass().equals(TicketGrantingTicket.class)) {
                if (properties.getIndexes().isEmpty() || properties.getIndexes().contains(INDEX_NAME_PRINCIPAL)) {
                    val principalIdIndex = new Index()
                        .on(MongoDbTicketDocument.FIELD_NAME_PRINCIPAL, Sort.Direction.ASC)
                        .named(INDEX_NAME_PRINCIPAL);
                    expectedIndexes.add(principalIdIndex);
                }
            }
            
            if (properties.getIndexes().isEmpty() || properties.getIndexes().contains(INDEX_NAME_EXPIRATION)) {
                val expireIndex = new Index()
                    .named(INDEX_NAME_EXPIRATION)
                    .on(MongoDbTicketDocument.FIELD_NAME_EXPIRE_AT, Sort.Direction.ASC);
                val timeout = ticket.getProperties().getStorageTimeout();
                if (timeout > 0 && timeout != Long.MAX_VALUE) {
                    expireIndex.expire(Duration.ofSeconds(timeout));
                }
                expectedIndexes.add(expireIndex);
            }

            if (!expectedIndexes.isEmpty()) {
                LOGGER.debug("Expected indexes are [{}]", expectedIndexes);
                MongoDbConnectionFactory.createOrUpdateIndexes(mongoTemplate, collection, expectedIndexes);
            }
        }
        return collection;
    }
}
