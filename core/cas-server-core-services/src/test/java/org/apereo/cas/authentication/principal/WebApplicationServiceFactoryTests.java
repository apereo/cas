package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link WebApplicationServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
class WebApplicationServiceFactoryTests {

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    protected ServiceFactory<WebApplicationService> serviceFactory;

    @Test
    void verifyServiceAttributes() {
        val request = new MockHttpServletRequest();
        request.addParameter("p1", "v1");
        request.addParameter("p2", "v2");
        request.addParameter(CasProtocolConstants.PARAMETER_PASSWORD, "m$hf74621");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "https://example.org?p3=v3&p4=v4");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val service = serviceFactory.createService(request);
        assertNotNull(service);
        assertEquals(7, service.getAttributes().size());
        assertTrue(service.getAttributes().containsKey("p1"));
        assertTrue(service.getAttributes().containsKey("p2"));
        assertTrue(service.getAttributes().containsKey("p3"));
        assertTrue(service.getAttributes().containsKey("p4"));
        assertTrue(service.getAttributes().containsKey("%s.requestURL".formatted(HttpServletRequest.class.getName())));
        assertTrue(service.getAttributes().containsKey("%s.localeName".formatted(HttpServletRequest.class.getName())));
        assertFalse(service.getAttributes().containsKey(CasProtocolConstants.PARAMETER_PASSWORD));
    }

    @Test
    void verifyServiceCreationSuccessfullyById() {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val service = serviceFactory.createService("testservice");
        assertNotNull(service);
    }

    @Test
    void verifyServiceCreationSuccessfullyByService() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
        val service = serviceFactory.createService(request);
        assertNotNull(service);
        assertEquals(CasProtocolConstants.PARAMETER_SERVICE, service.getSource());
    }

    @Test
    void verifyServiceCreationSuccessfullyByTargetService() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        val service = serviceFactory.createService(request);
        assertNotNull(service);
        assertEquals(CasProtocolConstants.PARAMETER_TARGET_SERVICE, service.getSource());
    }

    @Test
    void verifyServiceCreationSuccessfullyByTargetServiceAndTicket() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        request.addParameter(CasProtocolConstants.PARAMETER_METHOD, "post");
        val service = serviceFactory.createService(request);
        assertNotNull(service);
        assertEquals("ticket", service.getArtifactId());
    }

    @Test
    void verifyServiceCreationNoService() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        val service = serviceFactory.createService(request);
        assertNull(service);
    }

    @Test
    void verifyServiceCreationNoRequest() {
        val service = serviceFactory.createService("testservice");
        assertNotNull(service);
    }

    @Test
    void verifyServiceByClass() {
        assertThrows(ClassCastException.class, () -> serviceFactory.createService("testservice", mock(Service.class).getClass()));
        assertNotNull(serviceFactory.createService("testservice", WebApplicationService.class));
    }

    @Test
    void verifyServiceByClassReq() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        assertThrows(ClassCastException.class, () -> serviceFactory.createService(request, mock(Service.class).getClass()));
        assertNotNull(serviceFactory.createService(request, WebApplicationService.class));
    }
}
