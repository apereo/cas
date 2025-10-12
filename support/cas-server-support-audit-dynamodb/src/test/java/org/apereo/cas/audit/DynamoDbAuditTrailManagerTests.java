package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.config.CasSupportDynamoDbAuditAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.SdkSystemSetting;

/**
 * This is {@link DynamoDbAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    BaseAuditConfigurationTests.SharedTestConfiguration.class,
    CasSupportDynamoDbAuditAutoConfiguration.class
},
    properties = {
        "cas.audit.dynamo-db.endpoint=http://localhost:8000",
        "cas.audit.dynamo-db.drop-tables-on-startup=true",
        "cas.audit.dynamo-db.local-instance=true",
        "cas.audit.dynamo-db.region=us-east-1"
    }
)
@Tag("DynamoDb")
@ExtendWith(CasTestExtension.class)
@Getter
@EnabledIfListeningOnPort(port = 8000)
class DynamoDbAuditTrailManagerTests extends BaseAuditConfigurationTests {

    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }
    
    @Autowired
    @Qualifier("dynamoDbAuditTrailManager")
    private AuditTrailManager auditTrailManager;
}
