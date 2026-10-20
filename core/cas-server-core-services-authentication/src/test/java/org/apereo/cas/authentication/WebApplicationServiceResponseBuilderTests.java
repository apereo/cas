package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.SimpleUrlValidator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WebApplicationServiceResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Authentication")
class WebApplicationServiceResponseBuilderTests {

    @ParameterizedTest
    @EnumSource(Response.ResponseType.class)
    void verifyRejectsUserInfoInResponseDestinations(final Response.ResponseType responseType) {
        val serviceUrl = "https://trusted.example/callback";
        val unsafeUrl = "https://trusted.example%23@attacker.example/capture";
        val registeredService = new CasRegisteredService();
        registeredService.setResponseType(responseType.name());
        val service = new SimpleWebApplicationServiceImpl();
        service.setId(serviceUrl);
        service.setOriginalUrl(unsafeUrl);
        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(service)).thenReturn(registeredService);
        val builder = new WebApplicationServiceResponseBuilder(servicesManager, SimpleUrlValidator.getInstance());
        val authentication = mock(Authentication.class);

        assertThrows(UnauthorizedServiceException.class, () -> builder.build(service, "ST-test", authentication));

        service.setOriginalUrl(serviceUrl);
        service.getAttributes().put(Service.class.getName(), List.of(unsafeUrl));
        assertThrows(UnauthorizedServiceException.class, () -> builder.build(service, "ST-test", authentication));

        service.getAttributes().clear();
        registeredService.setRedirectUrl(unsafeUrl);
        assertThrows(UnauthorizedServiceException.class, () -> builder.build(service, "ST-test", authentication));

        registeredService.setRedirectUrl("https://example.org/callback%23@name");
        val response = builder.build(service, "ST-test", authentication);
        assertNotNull(response);
        assertTrue(response.url().startsWith(registeredService.getRedirectUrl()));
    }

    @Test
    void verifyServiceUrlRedirectOverride() {
        val registeredService = mock(CasModelRegisteredService.class);
        when(registeredService.getId()).thenReturn(1L);
        when(registeredService.getServiceId()).thenReturn("https://www.google.com/.+");
        when(registeredService.getRedirectUrl()).thenReturn("https://example.org");

        val service = mock(WebApplicationService.class);
        when(service.getId()).thenReturn("https://www.google.org");
        when(service.getOriginalUrl()).thenReturn("https://www.google.org");

        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val builder = new WebApplicationServiceResponseBuilder(servicesManager, SimpleUrlValidator.getInstance());
        val response = builder.build(service, "SERVICE_TICKET_ID", mock(Authentication.class));
        assertNotNull(response);
        assertEquals("https://example.org?ticket=SERVICE_TICKET_ID", response.url());
    }
}
