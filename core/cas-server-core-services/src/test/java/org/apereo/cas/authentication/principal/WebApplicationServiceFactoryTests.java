package org.apereo.cas.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * Test cases for {@link WebApplicationServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WebApplicationServiceFactoryTests {

    @Test
    public void verifyServiceCreationSuccessfullyById() {
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
    }

    @Test
    public void verifyServiceCreationSuccessfullyByTargetService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final WebApplicationService service = factory.createService(request);
        assertNotNull(service);
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
        assertEquals(service.getArtifactId(), "ticket");
    }

    @Test
    public void verifyServiceCreationNoService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();

        final WebApplicationService service = factory.createService(request);
        assertNull(service);
    }
}
