package org.apereo.cas.authentication.principal;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class WebApplicationServiceFactoryTests {

    @Test
    public void verifyServiceCreationSuccessfullyById() {
        final var factory = new WebApplicationServiceFactory();
        final var service = factory.createService("testservice");
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationSuccessfullyByService() {
        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
        final var factory = new WebApplicationServiceFactory();
        final var service = factory.createService(request);
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationSuccessfullyByTargetService() {
        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        final var factory = new WebApplicationServiceFactory();
        final var service = factory.createService(request);
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationSuccessfullyByTargetServiceAndTicket() {
        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        request.addParameter(CasProtocolConstants.PARAMETER_METHOD, "post");
        final var factory = new WebApplicationServiceFactory();
        final var service = factory.createService(request);
        assertNotNull(service);
        assertEquals("ticket", service.getArtifactId());
    }

    @Test
    public void verifyServiceCreationNoService() {
        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        final var factory = new WebApplicationServiceFactory();

        final var service = factory.createService(request);
        assertNull(service);
    }
}
