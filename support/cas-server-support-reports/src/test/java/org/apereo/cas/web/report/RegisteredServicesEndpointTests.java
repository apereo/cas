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
@Tag("ActuatorEndpoint")
public class RegisteredServicesEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("registeredServicesReportEndpoint")
    private RegisteredServicesEndpoint endpoint;

    @Test
    public void verifyOperation() {
        val service1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val service2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service1, service2);
        
        assertFalse(endpoint.handle().isEmpty());
        assertNotNull(endpoint.fetchService(service1.getServiceId()));
        assertNotNull(endpoint.deleteService(service1.getServiceId()));
        assertNull(endpoint.fetchService(String.valueOf(service1.getId())));

        assertNotNull(endpoint.deleteService(String.valueOf(service2.getId())));
        assertNull(endpoint.deleteService(String.valueOf(service2.getId())));
    }
}

