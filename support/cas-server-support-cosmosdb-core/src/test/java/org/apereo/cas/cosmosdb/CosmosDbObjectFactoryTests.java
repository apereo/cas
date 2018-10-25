package org.apereo.cas.cosmosdb;

import org.apereo.cas.configuration.model.support.cosmosdb.CosmosDbServiceRegistryProperties;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CosmosDbObjectFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AopAutoConfiguration.class)
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
        val factory = new CosmosDbObjectFactory(applicationContext);
        val properties = new CosmosDbServiceRegistryProperties();
        properties.setUri("http://localhost:1234");
        properties.setKey("123456");
        assertNotNull(factory.createDocumentClient(properties));
    }

    @Test
    public void verifyDocumentDb() {
        val factory = new CosmosDbObjectFactory(applicationContext);
        val properties = new CosmosDbServiceRegistryProperties();
        properties.setUri("http://localhost:1234");
        properties.setKey("123456");
        assertNotNull(factory.createDocumentDbFactory(properties));
    }

    @Test
    public void verifyDocumentDbTemplate() {
        val factory = new CosmosDbObjectFactory(applicationContext);
        val properties = new CosmosDbServiceRegistryProperties();
        properties.setUri("http://localhost:1234");
        properties.setKey("123456");
        assertNotNull(factory.createDocumentDbTemplate(factory.createDocumentDbFactory(properties), properties));
    }

}
