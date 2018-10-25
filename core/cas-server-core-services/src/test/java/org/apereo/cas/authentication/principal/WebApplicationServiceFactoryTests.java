package org.apereo.cas.authentication.principal;

import org.apereo.cas.CasProtocolConstants;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link WebApplicationServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WebApplicationServiceFactoryTests {

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
}
