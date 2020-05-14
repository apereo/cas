package org.apereo.cas.web.report;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServicesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = "management.endpoint.registeredServices.enabled=true")
@Tag("Simple")
public class RegisteredServicesEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("registeredServicesReportEndpoint")
    private RegisteredServicesEndpoint endpoint;

    @Test
    public void verifyOperation() {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        this.servicesManager.save(service);
        assertFalse(endpoint.handle().isEmpty());
        assertNotNull(endpoint.fetchService(service.getServiceId()));
        assertNotNull(endpoint.deleteService(service.getServiceId()));
        assertNull(endpoint.fetchService(String.valueOf(service.getId())));
    }
}

