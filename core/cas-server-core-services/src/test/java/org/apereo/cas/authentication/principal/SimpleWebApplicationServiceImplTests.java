package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServiceRegistry;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @since 3.1
 */
@Slf4j
public class SimpleWebApplicationServiceImplTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "simpleWebApplicationServiceImpl.json");

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SERVICE = "service";

    @Test
    public void verifySerializeACompletePrincipalToJson() throws IOException {
        final var request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE);
        final var serviceWritten = new WebApplicationServiceFactory().createService(request);
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        final var serviceRead = MAPPER.readValue(JSON_FILE, SimpleWebApplicationServiceImpl.class);
        assertEquals(serviceWritten, serviceRead);
    }

    @Test
    public void verifyResponse() {
        final var request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE);
        final var impl = new WebApplicationServiceFactory().createService(request);
        final var response = new WebApplicationServiceResponseBuilder(
            new DefaultServicesManager(mock(ServiceRegistry.class), mock(ApplicationEventPublisher.class)))
            .build(impl, "ticketId", RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
    }

    @Test
    public void verifyCreateSimpleWebApplicationServiceImplFromServiceAttribute() {
        final var request = new MockHttpServletRequest();
        request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, SERVICE);
        final var impl = new WebApplicationServiceFactory().createService(request);
        assertNotNull(impl);
    }

    @Test
    public void verifyResponseForJsession() {
        final var request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "http://www.cnn.com/;jsession=test");
        final var impl = new WebApplicationServiceFactory().createService(request);

        assertEquals("http://www.cnn.com/", impl.getId());
    }

    @Test
    public void verifyResponseWithNoTicket() {
        final var request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE);
        final var impl = new WebApplicationServiceFactory().createService(request);

        final var response = new WebApplicationServiceResponseBuilder(
            new DefaultServicesManager(mock(ServiceRegistry.class), mock(ApplicationEventPublisher.class)))
            .build(impl, null,
                RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertFalse(response.getUrl().contains("ticket="));
    }

    @Test
    public void verifyResponseWithNoTicketAndNoParameterInServiceURL() {
        final var request = new MockHttpServletRequest();
        request.setParameter(SERVICE, "http://foo.com/");
        final var impl = new WebApplicationServiceFactory().createService(request);
        final var response = new WebApplicationServiceResponseBuilder(
            new DefaultServicesManager(mock(ServiceRegistry.class), mock(ApplicationEventPublisher.class)))
            .build(impl, null,
                RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertFalse(response.getUrl().contains("ticket="));
        assertEquals("http://foo.com/", response.getUrl());
    }

    @Test
    public void verifyResponseWithNoTicketAndOneParameterInServiceURL() {
        final var request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "http://foo.com/?param=test");
        final var impl = new WebApplicationServiceFactory().createService(request);
        final var response = new WebApplicationServiceResponseBuilder(
            new DefaultServicesManager(mock(ServiceRegistry.class), mock(ApplicationEventPublisher.class)))
            .build(impl, null, RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertEquals("http://foo.com/?param=test", response.getUrl());
    }
}
