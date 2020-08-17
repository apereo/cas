package org.apereo.cas.web.report;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ImportRegisteredServicesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = "management.endpoint.importRegisteredServices.enabled=true")
@Tag("ActuatorEndpoint")
public class ImportRegisteredServicesEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("importRegisteredServicesEndpoint")
    private ImportRegisteredServicesEndpoint endpoint;

    @Test
    public void verifyOperationAsJson() throws Exception {
        val request = new MockHttpServletRequest();
        val content = new RegisteredServiceJsonSerializer().toString(RegisteredServiceTestUtils.getRegisteredService());
        request.setContent(content.getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.CREATED, endpoint.importService(request));
    }

    @Test
    public void verifyOperationAsYaml() throws Exception {
        val request = new MockHttpServletRequest();
        val content = new RegisteredServiceYamlSerializer().toString(RegisteredServiceTestUtils.getRegisteredService());
        request.setContent(content.getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.CREATED, endpoint.importService(request));
    }
}

