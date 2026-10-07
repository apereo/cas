package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.config.StringToWriteConcernConverter;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.mapping.Document;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbConnectionFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnabledIfListeningOnPort(port = 27017)
@Tag("MongoDb")
class MongoDbConnectionFactoryTests {
    private static final String URI = "mongodb://root:secret@localhost:27017/admin";

    @Test
    void verifyProps() {
        val factory = new MongoDbConnectionFactory();
        val props = new SingleCollectionMongoDbProperties();
        props.setClientUri(URI);
        val template = factory.buildMongoTemplate(props);
        assertNotNull(template);
        MongoDbConnectionFactory.createCollection(template, getClass().getSimpleName(), true);
    }

    @Test
    void verifyClient() {
        val props = new SingleCollectionMongoDbProperties();
        props.setClientUri(URI);
        val factory = new MongoDbConnectionFactory();
        val client = factory.buildMongoDbClient(props);
        assertNotNull(client);
    }

    @Test
    void verifyPackages() {
        val props = new SingleCollectionMongoDbProperties();
        props.setHost("localhost,localhost");
        props.setPort(27017);
        props.setUserId("root");
        props.setPassword("password");
        props.setDatabaseName("audit");
        props.setAuthenticationDatabaseName("admin");
        val factory = new MongoDbConnectionFactory(new StringToWriteConcernConverter()) {
            @Override
            protected Collection<String> getMappingBasePackages() {
                return List.of(SampleDocument.class.getPackageName());
            }
        };
        val template = factory.buildMongoTemplate(props);
        assertNotNull(template);
    }

    @Test
    void verifyIndexReplacedWithUniqueIndex() {
        val template = buildTemplate();
        val collectionName = "IndexReplacement" + UUID.randomUUID().toString().replace("-", "");
        MongoDbConnectionFactory.createCollection(template, collectionName, false);
        template.getCollection(collectionName).insertMany(List.of(
            new org.bson.Document("value", UUID.randomUUID().toString()),
            new org.bson.Document("value", UUID.randomUUID().toString())));
        try {
            MongoDbConnectionFactory.createOrUpdateIndexes(template, template.getCollection(collectionName),
                List.of(new Index().named("IDX_VALUE").on("value", Sort.Direction.ASC)));
            MongoDbConnectionFactory.createOrUpdateIndexes(template, template.getCollection(collectionName),
                List.of(new Index().named("IDX_VALUE").on("value", Sort.Direction.ASC).unique()));
            assertEquals(Boolean.TRUE, getIndex(template, collectionName).get("unique"));
        } finally {
            template.dropCollection(collectionName);
        }
    }

    @Test
    void verifyFailedIndexReplacementRestoresIndex() {
        val template = buildTemplate();
        val collectionName = "IndexReplacement" + UUID.randomUUID().toString().replace("-", "");
        MongoDbConnectionFactory.createCollection(template, collectionName, false);
        template.getCollection(collectionName).insertMany(List.of(
            new org.bson.Document("value", "duplicate"),
            new org.bson.Document("value", "duplicate")));
        try {
            MongoDbConnectionFactory.createOrUpdateIndexes(template, template.getCollection(collectionName),
                List.of(new Index().named("IDX_VALUE").on("value", Sort.Direction.ASC)));
            MongoDbConnectionFactory.createOrUpdateIndexes(template, template.getCollection(collectionName),
                List.of(new Index().named("IDX_VALUE").on("value", Sort.Direction.ASC).unique()));
            val index = getIndex(template, collectionName);
            assertEquals(new org.bson.Document("value", 1), index.get("key"));
            assertNull(index.get("unique"));
        } finally {
            template.dropCollection(collectionName);
        }
    }

    private static MongoOperations buildTemplate() {
        val props = new SingleCollectionMongoDbProperties();
        props.setClientUri(URI);
        return new MongoDbConnectionFactory().buildMongoTemplate(props);
    }

    private static org.bson.Document getIndex(final MongoOperations template, final String collectionName) {
        return template.getCollection(collectionName).listIndexes().into(new ArrayList<>())
            .stream()
            .filter(index -> "IDX_VALUE".equals(index.getString("name")))
            .findFirst()
            .orElseThrow();
    }

    @Document
    static class SampleDocument {}
}
