package org.apereo.cas.authentication.principal;

import org.apereo.cas.CasProtocolConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link WebApplicationServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Tag("Authentication")
public class WebApplicationServiceFactoryTests {

    @Test
    public void verifyServiceAttributes() {
        val request = new MockHttpServletRequest();
        request.addParameter("p1", "v1");
        request.addParameter("p2", "v2");
        request.addParameter(CasProtocolConstants.PARAMETER_PASSWORD, "m$hf74621");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "https://example.org?p3=v3&p4=v4");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(request);
        assertNotNull(service);
        assertEquals(4, service.getAttributes().size());
        assertTrue(service.getAttributes().containsKey("p1"));
        assertTrue(service.getAttributes().containsKey("p2"));
        assertTrue(service.getAttributes().containsKey("p3"));
        assertTrue(service.getAttributes().containsKey("p4"));
        assertFalse(service.getAttributes().containsKey(CasProtocolConstants.PARAMETER_PASSWORD));
    }

    @Test
    public void verifyServiceCreationSuccessfullyById() {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService("testservice");
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationSuccessfullyByService() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(request);
        assertNotNull(service);
        assertEquals(CasProtocolConstants.PARAMETER_SERVICE, service.getSource());
    }

    @Test
    public void verifyServiceCreationSuccessfullyByTargetService() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(request);
        assertNotNull(service);
        assertEquals(CasProtocolConstants.PARAMETER_TARGET_SERVICE, service.getSource());
    }

    @Test
    public void verifyServiceCreationSuccessfullyByTargetServiceAndTicket() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        request.addParameter(CasProtocolConstants.PARAMETER_METHOD, "post");
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(request);
        assertNotNull(service);
        assertEquals("ticket", service.getArtifactId());
    }

    @Test
    public void verifyServiceCreationNoService() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        val factory = new WebApplicationServiceFactory();

        val service = factory.createService(request);
        assertNull(service);
    }

    @Test
    public void verifyServiceCreationNoRequest() {
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService("testservice");
        assertNotNull(service);
    }

    @Test
    public void verifyServiceByClass() {
        val factory = new WebApplicationServiceFactory();
        assertThrows(ClassCastException.class, () -> factory.createService("testservice", mock(Service.class).getClass()));
        assertNotNull(factory.createService("testservice", WebApplicationService.class));
    }

    @Test
    public void verifyServiceByClassReq() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        val factory = new WebApplicationServiceFactory();
        assertThrows(ClassCastException.class, () -> factory.createService(request, mock(Service.class).getClass()));
        assertNotNull(factory.createService(request, WebApplicationService.class));
    }
}
