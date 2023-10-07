package org.apereo.cas.web.report;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuditLogEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "management.endpoint.auditLog.enabled=true",
    "cas.audit.engine.number-of-days-in-history=30"
})
@Tag("ActuatorEndpoint")
class AuditLogEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("auditLogEndpoint")
    private AuditLogEndpoint auditLogEndpoint;

    @Test
    void verifyOperation() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val results = auditLogEndpoint.getAuditLog(StringUtils.EMPTY);
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyOperationByInterval() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val results = auditLogEndpoint.getAuditLog("PT10M");
        assertFalse(results.isEmpty());
    }
}
