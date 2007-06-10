/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import junit.framework.TestCase;

import org.jasig.cas.authentication.principal.Response.ResponseType;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * 
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class SimpleWebApplicationServiceImplTests extends TestCase {

    public void testResponse() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "service");
        final SimpleWebApplicationServiceImpl impl = SimpleWebApplicationServiceImpl.createServiceFrom(request);
        
        final Response response = impl.getResponse("ticketId");
        assertNotNull(response);
        assertEquals(ResponseType.REDIRECT, response.getResponseType());
    }
    
    public void testResponseForJsession() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "http://www.cnn.com/;jsession=test");
        final WebApplicationService impl = SimpleWebApplicationServiceImpl.createServiceFrom(request);
        
        assertEquals("http://www.cnn.com/", impl.getId());
    }
    
    public void testResponseWithNoTicket() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "service");
        final WebApplicationService impl = SimpleWebApplicationServiceImpl.createServiceFrom(request);
        
        final Response response = impl.getResponse(null);
        assertNotNull(response);
        assertEquals(ResponseType.REDIRECT, response.getResponseType());
        assertFalse(response.getUrl().contains("ticket="));
    }
    
    public void testResponseWithNoTicketAndNoParameterInServiceURL() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "http://foo.com/");
        final WebApplicationService impl = SimpleWebApplicationServiceImpl.createServiceFrom(request);
        
        final Response response = impl.getResponse(null);
        assertNotNull(response);
        assertEquals(ResponseType.REDIRECT, response.getResponseType());
        assertFalse(response.getUrl().contains("ticket="));
        assertEquals("http://foo.com/",response.getUrl());
    }
    
    public void testResponseWithNoTicketAndOneParameterInServiceURL() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "http://foo.com/?param=test");
        final WebApplicationService impl = SimpleWebApplicationServiceImpl.createServiceFrom(request);
        
        final Response response = impl.getResponse(null);
        assertNotNull(response);
        assertEquals(ResponseType.REDIRECT, response.getResponseType());
        assertEquals("http://foo.com/?param=test",response.getUrl());
    }
}
