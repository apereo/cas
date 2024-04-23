package org.apereo.cas;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MongoDbPropertySourceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MongoDb")
@EnabledIfListeningOnPort(port = 27017)
class MongoDbPropertySourceLocatorTests {

    @Test
    void verifyOperation() throws Throwable {
        val factory = new SimpleMongoClientDatabaseFactory(MongoDbCloudConfigBootstrapAutoConfigurationTests.MONGODB_URI);
        val template = new MongoTemplate(factory);
        val loc = new MongoDbPropertySourceLocator(template);
        assertNull(loc.locate(mock(Environment.class)));
    }

}
