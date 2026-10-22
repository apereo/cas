package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.config.CasSupportDynamoDbAuditAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.SdkSystemSetting;
import static org.junit.jupiter.api.Assertions.*;

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
        "cas.audit.dynamo-db.region=us-east-1",
        "cas.audit.dynamo-db.asynchronous=false"
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

    @Test
    void verifyRecordsOfPrincipalAccumulateWithTheirTime() {
        val principal = UUID.randomUUID().toString();
        val now = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);
        val clientInfo = new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London");
        auditTrailManager.record(new AuditActionContext(principal, "first", "TEST", "CAS", now.minusMinutes(5), clientInfo));
        auditTrailManager.record(new AuditActionContext(principal, "second", "TEST", "CAS", now, clientInfo));
        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(
            AuditTrailManager.WhereClauseFields.DATE, now.minusMinutes(1),
            AuditTrailManager.WhereClauseFields.PRINCIPAL, principal);
        val recent = auditTrailManager.getAuditRecords(criteria);
        assertEquals(1, recent.size());
        assertEquals("second", recent.getFirst().getResourceOperatedUpon());
        assertEquals(now, recent.getFirst().getWhenActionWasPerformed());
        val all = auditTrailManager.getAuditRecords(Map.of(
            AuditTrailManager.WhereClauseFields.DATE, now.minusMinutes(10),
            AuditTrailManager.WhereClauseFields.PRINCIPAL, principal));
        assertEquals(2, all.size());
    }
}
