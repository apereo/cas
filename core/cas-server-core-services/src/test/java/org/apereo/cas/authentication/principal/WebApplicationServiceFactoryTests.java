package org.apereo.cas.authentication.principal;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasProtocolConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.*;

/**
 * Test cases for {@link WebApplicationServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class WebApplicationServiceFactoryTests {

    @Test
    public void verifyServiceCreationSuccessfullyById() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final WebApplicationService service = factory.createService("testservice");
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationSuccessfullyByService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final WebApplicationService service = factory.createService(request);
        assertNotNull(service);
        assertEquals(CasProtocolConstants.PARAMETER_SERVICE, service.getSource());
    }

    @Test
    public void verifyServiceCreationSuccessfullyByTargetService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final WebApplicationService service = factory.createService(request);
        assertNotNull(service);
        assertEquals(CasProtocolConstants.PARAMETER_TARGET_SERVICE, service.getSource());
    }

    @Test
    public void verifyServiceCreationSuccessfullyByTargetServiceAndTicket() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        request.addParameter(CasProtocolConstants.PARAMETER_METHOD, "post");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final WebApplicationService service = factory.createService(request);
        assertNotNull(service);
        assertEquals("ticket", service.getArtifactId());
    }

    @Test
    public void verifyServiceCreationNoService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();

        final WebApplicationService service = factory.createService(request);
        assertNull(service);
    }

    @Test
    public void verifyServiceCreationNoRequest() {
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();

        final WebApplicationService service = factory.createService("testservice");
        assertNotNull(service);
    }
}
