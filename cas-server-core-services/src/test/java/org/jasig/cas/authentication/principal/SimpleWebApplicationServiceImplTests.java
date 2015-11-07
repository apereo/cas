package org.jasig.cas.authentication.principal;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @since 3.1
 *
 */
public class SimpleWebApplicationServiceImplTests {

    @Test
    public void verifyResponse() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "service");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);

        final Response response = impl.getResponse("ticketId");
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
    }

    @Test
    public void verifyCreateSimpleWebApplicationServiceImplFromServiceAttribute() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("service", "service");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);
        assertNotNull(impl);
    }

    @Test
    public void verifyResponseForJsession() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "http://www.cnn.com/;jsession=test");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);

        assertEquals("http://www.cnn.com/", impl.getId());
    }

    @Test
    public void verifyResponseWithNoTicket() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "service");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);

        final Response response = impl.getResponse(null);
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertFalse(response.getUrl().contains("ticket="));
    }

    @Test
    public void verifyResponseWithNoTicketAndNoParameterInServiceURL() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "http://foo.com/");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);

        final Response response = impl.getResponse(null);
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertFalse(response.getUrl().contains("ticket="));
        assertEquals("http://foo.com/", response.getUrl());
    }

    @Test
    public void verifyResponseWithNoTicketAndOneParameterInServiceURL() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "http://foo.com/?param=test");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);

        final Response response = impl.getResponse(null);
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertEquals("http://foo.com/?param=test", response.getUrl());
    }
}
