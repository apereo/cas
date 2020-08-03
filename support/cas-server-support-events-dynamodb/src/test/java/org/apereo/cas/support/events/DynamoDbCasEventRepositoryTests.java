package org.apereo.cas.support.events;

import org.apereo.cas.config.CasEventsDynamoDbRepositoryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import software.amazon.awssdk.core.SdkSystemSetting;

/**
 * This is {@link DynamoDbCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("DynamoDb")
@SpringBootTest(classes = {
    CasEventsDynamoDbRepositoryConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.events.dynamo-db.endpoint=http://localhost:8000",
        "cas.events.dynamo-db.drop-tables-on-startup=true",
        "cas.events.dynamo-db.local-instance=true",
        "cas.events.dynamo-db.region=us-east-1"
    })
@Getter
@EnabledIfPortOpen(port = 8000)
public class DynamoDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository eventRepository;

    @Autowired
    @Qualifier("dynamoDbCasEventsFacilitator")
    private DynamoDbCasEventsFacilitator dynamoDbCasEventsFacilitator;

    @BeforeEach
    public void beforeEach() {
        dynamoDbCasEventsFacilitator.deleteAll();
    }
}
