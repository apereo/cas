package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentDynamoDbAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.SdkSystemSetting;

/**
 * This is {@link DynamoDbConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasConsentDynamoDbAutoConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.consent.dynamo-db.endpoint=http://localhost:8000",
        "cas.consent.dynamo-db.drop-tables-on-startup=true",
        "cas.consent.dynamo-db.local-instance=true",
        "cas.consent.dynamo-db.region=us-east-1"
    })
@Tag("DynamoDb")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 8000)
@Getter
class DynamoDbConsentRepositoryTests extends BaseConsentRepositoryTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Autowired
    @Qualifier(ConsentRepository.BEAN_NAME)
    protected ConsentRepository repository;

}
