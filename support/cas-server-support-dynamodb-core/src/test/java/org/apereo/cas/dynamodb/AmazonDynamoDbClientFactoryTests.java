package org.apereo.cas.dynamodb;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;

import com.amazonaws.regions.Regions;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonDynamoDbClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("dynamodb")
public class AmazonDynamoDbClientFactoryTests {

    @Test
    public void verifyAction() {
        val factory = new AmazonDynamoDbClientFactory();
        val properties = new AbstractDynamoDbProperties() {
            private static final long serialVersionUID = -3599433486448467450L;
        };
        properties.setRegion(Regions.US_EAST_1.getName());
        val client = factory.createAmazonDynamoDb(properties);
        assertNotNull(client);
    }
}
