package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.configuration.model.support.mongo.ticketregistry.MongoDbTicketRegistryProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.MongoDbTicketDocument;
import org.apereo.cas.ticket.registry.MongoDbTicketRegistry;
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
     * Index on the ticket id field that earlier versions stored next to a generated document id.
     * The ticket id is the document id now, so this index is no longer created and is removed when found.
     * When/if changing index names, be sure to also update the field
     * documentation that lists all supported indexes.
     */
    private static final String INDEX_NAME_ID = "IDX_ID";

    private static final String LEGACY_FIELD_NAME_ID = "ticketId";
    private static final String INDEX_NAME_PRINCIPAL = "IDX_PRINCIPAL";
    private static final String INDEX_NAME_EXPIRATION = "IDX_EXPIRATION";
    private static final String INDEX_NAME_SERVICE = "IDX_SERVICE";
    private static final String INDEX_NAME_ATTRIBUTES = "IDX_ATTRIBUTES";

    private final TicketCatalog ticketCatalog;

    private final MongoOperations mongoTemplate;

    private final MongoDbTicketRegistryProperties properties;
    
    /**
     * Create ticket collections. This honors {@code drop-collection} and {@code drop-indexes},
     * both of which destroy data, so it must be invoked exactly once per application context and
     * never from a refresh-scoped bean that is rebuilt on every configuration refresh.
     */
    public void createTicketCollections() {
        val sessionCollections = getCollectionNames(MongoDbTicketRegistry.getSessionTicketDefinitions(ticketCatalog));
        val serviceCollections = getCollectionNames(MongoDbTicketRegistry.getServiceTicketDefinitions(ticketCatalog));
        getCollectionNames(ticketCatalog.findAll().stream()).forEach(collectionName -> {
            val collection = createTicketCollection(collectionName,
                sessionCollections.contains(collectionName), serviceCollections.contains(collectionName));
            LOGGER.debug("Created MongoDb collection configuration for [{}]", collection.getNamespace().getFullName());
        });
        LOGGER.info("Configured MongoDb Ticket Registry instance with available collections: [{}]", mongoTemplate.getCollectionNames());
    }

    /**
     * Convert documents written by earlier versions, which kept the ticket id in a {@code ticketId} field
     * next to a generated {@code ObjectId}, so that the ticket id becomes the document id. Only documents
     * whose id is an {@code ObjectId} are touched, so this is safe to run while the registry is in use
     * and to run again after an interruption.
     */
    public void migrateTicketDocuments() {
        getCollectionNames(ticketCatalog.findAll().stream())
            .forEach(collectionName -> migrateLegacyDocuments(mongoTemplate.getCollection(collectionName)));
    }

    private static void migrateLegacyDocuments(final MongoCollection<Document> collection) {
        val legacyDocuments = new Document(MongoDbTicketDocument.FIELD_NAME_ID, new Document("$type", "objectId"));
        if (collection.find(legacyDocuments).projection(new Document(MongoDbTicketDocument.FIELD_NAME_ID, 1)).limit(1).first() == null) {
            return;
        }
        val collectionName = collection.getNamespace().getCollectionName();
        LOGGER.info("Converting ticket documents in [{}] to use the ticket id as the document id", collectionName);
        collection.aggregate(List.of(
            new Document("$match", new Document(legacyDocuments).append(LEGACY_FIELD_NAME_ID, new Document("$type", "string"))),
            new Document("$set", new Document(MongoDbTicketDocument.FIELD_NAME_ID, "$" + LEGACY_FIELD_NAME_ID)),
            new Document("$unset", LEGACY_FIELD_NAME_ID),
            new Document("$merge", new Document("into", collectionName)
                .append("on", MongoDbTicketDocument.FIELD_NAME_ID)
                .append("whenMatched", "keepExisting")
                .append("whenNotMatched", "insert")))).toCollection();
        val converted = collection.deleteMany(legacyDocuments).getDeletedCount();
        LOGGER.info("Converted [{}] ticket document(s) in [{}]", converted, collectionName);
    }

    private static List<String> getCollectionNames(final Stream<TicketDefinition> definitions) {
        return definitions
            .map(definition -> definition.getProperties().getStorageName())
            .distinct()
            .toList();
    }

    private MongoCollection<Document> createTicketCollection(final String collectionName,
                                                             final boolean sessionCollection,
                                                             final boolean serviceCollection) {
        LOGGER.trace("Setting up MongoDb Ticket Registry instance [{}]", collectionName);
        MongoDbConnectionFactory.createCollection(mongoTemplate, collectionName, properties.isDropCollection());

        val collection = mongoTemplate.getCollection(collectionName);
        migrateLegacyDocuments(collection);
        if (properties.isUpdateIndexes()) {
            if (properties.isDropIndexes()) {
                LOGGER.trace("Dropping existing indexes on collection [{}]...", collectionName);
                MongoDbConnectionFactory.dropCollectionIndexes(collection);
            }
            val expectedIndexes = new ArrayList<IndexDefinition>();

            if (properties.getIndexes().isEmpty() || properties.getIndexes().contains(INDEX_NAME_PRINCIPAL)) {
                val principalIdIndex = new Index()
                    .on(MongoDbTicketDocument.FIELD_NAME_PRINCIPAL, Sort.Direction.ASC)
                    .named(INDEX_NAME_PRINCIPAL);
                expectedIndexes.add(principalIdIndex);
            }

            if (properties.getIndexes().isEmpty() || properties.getIndexes().contains(INDEX_NAME_EXPIRATION)) {
                val expireIndex = new Index()
                    .named(INDEX_NAME_EXPIRATION)
                    .on(MongoDbTicketDocument.FIELD_NAME_EXPIRE_AT, Sort.Direction.ASC)
                    .expire(Duration.ZERO);
                expectedIndexes.add(expireIndex);
            }

            if (serviceCollection && (properties.getIndexes().isEmpty() || properties.getIndexes().contains(INDEX_NAME_SERVICE))) {
                val serviceIndex = new Index()
                    .named(INDEX_NAME_SERVICE)
                    .on(MongoDbTicketDocument.FIELD_NAME_SERVICE, Sort.Direction.ASC);
                expectedIndexes.add(serviceIndex);
            }

            if (sessionCollection && (properties.getIndexes().isEmpty() || properties.getIndexes().contains(INDEX_NAME_ATTRIBUTES))) {
                val attributeIndex = new Index()
                    .named(INDEX_NAME_ATTRIBUTES)
                    .on(MongoDbTicketDocument.FIELD_NAME_ATTRIBUTES + ".$**", Sort.Direction.ASC);
                expectedIndexes.add(attributeIndex);
            }

            val unusedIndexes = new ArrayList<>(List.of(INDEX_NAME_ID));
            if (!serviceCollection) {
                unusedIndexes.add(INDEX_NAME_SERVICE);
            }
            if (!sessionCollection) {
                unusedIndexes.add(INDEX_NAME_ATTRIBUTES);
            }
            dropIndexes(collection, unusedIndexes);

            if (!expectedIndexes.isEmpty()) {
                LOGGER.debug("Expected indexes are [{}]", expectedIndexes);
                MongoDbConnectionFactory.createOrUpdateIndexes(mongoTemplate, collection, expectedIndexes);
            }
        }
        return collection;
    }

    private static void dropIndexes(final MongoCollection<Document> collection, final List<String> indexNames) {
        if (indexNames.isEmpty()) {
            return;
        }
        val existingIndexes = collection.listIndexes().map(index -> index.getString("name")).into(new HashSet<>());
        indexNames.stream().filter(existingIndexes::contains).forEach(indexName -> {
            try {
                collection.dropIndex(indexName);
                LOGGER.info("Dropped index [{}] from collection [{}]; no query uses it there", indexName, collection.getNamespace());
            } catch (final Exception e) {
                LoggingUtils.warn(LOGGER, e);
            }
        });
    }
}
