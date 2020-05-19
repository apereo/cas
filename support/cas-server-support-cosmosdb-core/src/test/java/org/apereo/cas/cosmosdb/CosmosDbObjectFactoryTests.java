package org.apereo.cas.cosmosdb;

import org.apereo.cas.configuration.model.support.cosmosdb.CosmosDbServiceRegistryProperties;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CosmosDbObjectFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Tag("Simple")
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
        properties.setKey(EncodingUtils.encodeBase64("123456"));
        assertNotNull(factory.createDocumentClient(properties));
    }

    @Test
    public void verifyDocumentDb() {
        val factory = new CosmosDbObjectFactory(applicationContext);
        val properties = new CosmosDbServiceRegistryProperties();
        properties.setUri("http://localhost:1234");
        properties.setKey(EncodingUtils.encodeBase64("123456"));
        assertNotNull(factory.createDocumentDbFactory(properties));
    }

    @Test
    public void verifyDocumentDbTemplate() {
        val factory = new CosmosDbObjectFactory(applicationContext);
        val properties = new CosmosDbServiceRegistryProperties();
        properties.setUri("http://localhost:1234");
        properties.setKey(EncodingUtils.encodeBase64("123456"));
        assertNotNull(factory.createDocumentDbTemplate(factory.createDocumentDbFactory(properties), properties));
    }

}
