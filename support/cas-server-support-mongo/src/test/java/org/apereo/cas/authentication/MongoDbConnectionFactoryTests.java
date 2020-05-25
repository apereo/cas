package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbConnectionFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnabledIfPortOpen(port = 27017)
@Tag("MongoDb")
public class MongoDbConnectionFactoryTests {
    private static final String URI = "mongodb://root:secret@localhost:27017/admin";

    @Test
    public void verifyProps() {
        val factory = new MongoDbConnectionFactory();
        val props = new SingleCollectionMongoDbProperties();
        props.setClientUri(URI);
        val template = factory.buildMongoTemplate(props);
        assertNotNull(template);
        factory.createCollection(template, getClass().getSimpleName(), true);
    }

    @Test
    public void verifyClient() {
        val props = new SingleCollectionMongoDbProperties();
        props.setClientUri(URI);
        val factory = new MongoDbConnectionFactory();
        val client = factory.buildMongoDbClient(props);
        assertNotNull(client);
    }
}
