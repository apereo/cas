package org.apereo.cas.web.report;

import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
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
    "cas.audit.number-of-days-in-history=30"
})
@Tag("ActuatorEndpoint")
public class AuditLogEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("auditLogEndpoint")
    private AuditLogEndpoint auditLogEndpoint;

    @BeforeAll
    public static void setup() {
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(RegisteredServiceTestUtils.getAuthentication("casuser"));
    }

    @Test
    public void verifyOperation() {
        this.servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()));
        val results = auditLogEndpoint.getAuditLog(StringUtils.EMPTY);
        assertFalse(results.isEmpty());
    }

    @Test
    public void verifyOperationByInterval() {
        this.servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()));
        val results = auditLogEndpoint.getAuditLog("PT10M");
        assertFalse(results.isEmpty());
    }

    @Test
    public void verifyOperationByIntervalAndUser() {
        this.servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()));
        val results = auditLogEndpoint.getAuditLog("PT10M", null, null, "casuser", null);
        assertFalse(results.isEmpty());
    }
}
