package org.apereo.cas.support.events;

import org.apereo.cas.config.CasEventsDynamoDbRepositoryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.SdkSystemSetting;

/**
 * This is {@link DynamoDbCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("DynamoDb")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasEventsDynamoDbRepositoryAutoConfiguration.class, properties = {
    "cas.events.dynamo-db.endpoint=http://localhost:8000",
    "cas.events.dynamo-db.drop-tables-on-startup=true",
    "cas.events.dynamo-db.local-instance=true",
    "cas.events.dynamo-db.region=us-east-1"
})
@Getter
@EnabledIfListeningOnPort(port = 8000)
class DynamoDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository eventRepository;

    @Autowired
    @Qualifier("dynamoDbCasEventsFacilitator")
    private DynamoDbCasEventsFacilitator dynamoDbCasEventsFacilitator;

    @BeforeEach
    void beforeEach() throws Exception {
        dynamoDbCasEventsFacilitator.deleteAll();
    }
}
