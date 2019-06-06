package org.apereo.cas.web.report;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
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
@TestPropertySource(properties = "management.endpoint.auditLog.enabled=true")
public class AuditLogEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("auditLogEndpoint")
    private AuditLogEndpoint auditLogEndpoint;

    @Test
    public void verifyOperation() {
        this.servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()));
        val results = auditLogEndpoint.getAuditLog();
        assertFalse(results.isEmpty());
    }
}
