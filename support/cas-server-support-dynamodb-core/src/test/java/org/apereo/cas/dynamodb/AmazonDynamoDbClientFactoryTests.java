package org.apereo.cas.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link AmazonDynamoDbClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AmazonDynamoDbClientFactoryTests {

    @Test
    public void verifyAction() {
        final AmazonDynamoDbClientFactory factory = new AmazonDynamoDbClientFactory();
        final AbstractDynamoDbProperties properties = new AbstractDynamoDbProperties() {
            private static final long serialVersionUID = -3599433486448467450L;
        };
        final AmazonDynamoDB client = factory.createAmazonDynamoDb(properties);
        assertNotNull(client);
    }
}
