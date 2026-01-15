package org.apereo.cas.dynamodb;

import module java.base;
import org.apereo.cas.configuration.model.support.dynamodb.AuditDynamoDbProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.dax.ClusterDaxClient;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonDynamoDbClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("DynamoDb")
class AmazonDynamoDbClientFactoryTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Test
    void verifyClientCreation() {
        val factory = new AmazonDynamoDbClientFactory();
        val properties = new AuditDynamoDbProperties();
        properties.setRegion(Region.US_EAST_1.id());
        val client = factory.createAmazonDynamoDb(properties);
        assertNotNull(client);
    }

    @Test
    void verifyDax() {
        val factory = new AmazonDynamoDbClientFactory();
        val properties = new AuditDynamoDbProperties();
        properties.setRegion(Region.US_EAST_1.id());
        properties.getDax().setUrl("dax://example.fake.us-east-1.amazonaws.com");
        val client = factory.createAmazonDynamoDb(properties);
        assertInstanceOf(ClusterDaxClient.class, client);
    }
}
