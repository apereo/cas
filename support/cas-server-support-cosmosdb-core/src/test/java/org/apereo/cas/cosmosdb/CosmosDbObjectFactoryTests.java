package org.apereo.cas.cosmosdb;

import org.apereo.cas.configuration.model.support.cosmosdb.CosmosDbServiceRegistryProperties;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link CosmosDbObjectFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class CosmosDbObjectFactoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

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
