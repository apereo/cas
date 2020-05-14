package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WebApplicationServiceResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class WebApplicationServiceResponseBuilderTests {

    @Test
    public void verifyServiceUrlRedirectOverride() {
        val registeredService = mock(RegisteredService.class);
        when(registeredService.getId()).thenReturn(1L);
        when(registeredService.getServiceId()).thenReturn("https://www.google.com/.+");
        when(registeredService.getRedirectUrl()).thenReturn("https://example.org");

        val service = mock(WebApplicationService.class);
        when(service.getId()).thenReturn("https://www.google.org");
        when(service.getOriginalUrl()).thenReturn("https://www.google.org");

        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        
        val builder = new WebApplicationServiceResponseBuilder(servicesManager);
        val response = builder.build(service, "SERVICE_TICKET_ID", mock(Authentication.class));
        assertNotNull(response);
        assertEquals("https://example.org?ticket=SERVICE_TICKET_ID", response.getUrl());
    }
}
