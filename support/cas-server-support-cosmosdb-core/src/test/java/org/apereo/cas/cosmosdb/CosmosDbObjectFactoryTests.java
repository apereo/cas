package org.apereo.cas.cosmosdb;

import org.apereo.cas.configuration.model.support.cosmosdb.CosmosDbServiceRegistryProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link CosmosDbObjectFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class CosmosDbObjectFactoryTests {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void verifyDatabaseLinkCollections() {
        assertNotNull(CosmosDbObjectFactory.getDatabaseLink("databaseName"));
        assertNotNull(CosmosDbObjectFactory.getDocumentLink("databaseName", "document", "id"));
        assertNotNull(CosmosDbObjectFactory.getCollectionLink("databaseName", "collection"));
    }

    @Test
    public void verifyClient() {
        final CosmosDbObjectFactory factory = new CosmosDbObjectFactory(applicationContext);
        final CosmosDbServiceRegistryProperties properties = new CosmosDbServiceRegistryProperties();
        properties.setUri("http://localhost:1234");
        properties.setKey("123456");
        assertNotNull(factory.createDocumentClient(properties));
    }

    @Test
    public void verifyDocumentDb() {
        final CosmosDbObjectFactory factory = new CosmosDbObjectFactory(applicationContext);
        final CosmosDbServiceRegistryProperties properties = new CosmosDbServiceRegistryProperties();
        properties.setUri("http://localhost:1234");
        properties.setKey("123456");
        assertNotNull(factory.createDocumentDbFactory(properties));
    }

    @Test
    public void verifyDocumentDbTemplate() {
        final CosmosDbObjectFactory factory = new CosmosDbObjectFactory(applicationContext);
        final CosmosDbServiceRegistryProperties properties = new CosmosDbServiceRegistryProperties();
        properties.setUri("http://localhost:1234");
        properties.setKey("123456");
        assertNotNull(factory.createDocumentDbTemplate(factory.createDocumentDbFactory(properties), properties));
    }

}
