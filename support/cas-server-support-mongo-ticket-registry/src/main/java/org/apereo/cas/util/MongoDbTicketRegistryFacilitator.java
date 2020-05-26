package org.apereo.cas.util;

import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.TicketHolder;

import com.google.common.collect.ImmutableSet;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

import java.time.Duration;

/**
 * This is {@link MongoDbTicketRegistryFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class MongoDbTicketRegistryFacilitator {
    private static final ImmutableSet<String> MONGO_INDEX_KEYS = ImmutableSet.of("v", "key", "name", "ns");

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

        LOGGER.trace("Creating indices on collection [{}] to auto-expire documents...", collectionName);
        val collection = mongoTemplate.getCollection(collectionName);
        val index = new Index().on(TicketHolder.FIELD_NAME_EXPIRE_AT, Sort.Direction.ASC)
            .expire(Duration.ofSeconds(ticket.getProperties().getStorageTimeout()));
        removeDifferingIndexIfAny(collection, index);
        mongoTemplate.indexOps(collectionName).ensureIndex(index);

        val textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
            .onField(TicketHolder.FIELD_NAME_JSON)
            .onField(TicketHolder.FIELD_NAME_TYPE)
            .build();
        mongoTemplate.indexOps(collectionName).ensureIndex(textIndex);

        return collection;
    }

    /**
     * Remove any index with the same indexKey but differing indexOptions in anticipation of recreating it.
     *
     * @param collection The collection to check the indexes of
     * @param index      The index to find
     */
    private static void removeDifferingIndexIfAny(final MongoCollection collection, final Index index) {
        val indexes = (ListIndexesIterable<Document>) collection.listIndexes();
        var indexExistsWithDifferentOptions = false;

        val indexKeys = index.getIndexKeys();
        val indexOptions = index.getIndexOptions();
        for (val existingIndex : indexes) {
            val keyMatches = existingIndex.get("key").equals(indexKeys);
            val optionsMatch = indexOptions.entrySet().stream()
                .allMatch(entry -> entry.getValue().equals(existingIndex.get(entry.getKey())));
            val noExtraOptions = existingIndex.keySet().stream()
                .allMatch(key -> MONGO_INDEX_KEYS.contains(key) || indexOptions.keySet().contains(key));
            indexExistsWithDifferentOptions |= keyMatches && !(optionsMatch && noExtraOptions);
        }

        if (indexExistsWithDifferentOptions) {
            LOGGER.debug("Removing MongoDb index [{}] from [{}]", indexKeys, collection.getNamespace());
            collection.dropIndex(indexKeys);
        }
    }
}
