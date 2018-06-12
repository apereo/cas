package org.apereo.cas.dynamodb;

import org.apereo.cas.category.DynamoDbCategory;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/**
 * This is {@link AmazonDynamoDbClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(DynamoDbCategory.class)
public class AmazonDynamoDbClientFactoryTests {

    @Test
    public void verifyAction() {
        final var factory = new AmazonDynamoDbClientFactory();
        final AbstractDynamoDbProperties properties = new AbstractDynamoDbProperties() {
            private static final long serialVersionUID = -3599433486448467450L;
        };
        final var client = factory.createAmazonDynamoDb(properties);
        assertNotNull(client);
    }
}
