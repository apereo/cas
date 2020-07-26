package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasSupportDynamoDbAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import software.amazon.awssdk.core.SdkSystemSetting;

/**
 * This is {@link DynamoDbAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasCoreAuditConfiguration.class,
    CasSupportDynamoDbAuditConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreWebConfiguration.class},
    properties = {
        "cas.audit.dynamo-db.endpoint=http://localhost:8000",
        "cas.audit.dynamo-db.drop-tables-on-startup=true",
        "cas.audit.dynamo-db.local-instance=true",
        "cas.audit.dynamo-db.region=us-east-1",
        "cas.audit.dynamo-db.asynchronous=false"
    }
)
@Tag("DynamoDb")
@Getter
@EnabledIfPortOpen(port = 8000)
public class DynamoDbAuditTrailManagerTests extends BaseAuditConfigurationTests {

    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }
    
    @Autowired
    @Qualifier("dynamoDbAuditTrailManager")
    private AuditTrailManager auditTrailManager;
}
