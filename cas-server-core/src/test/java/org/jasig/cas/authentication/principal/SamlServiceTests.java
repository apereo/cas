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
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class SamlServiceTests extends TestCase {

    public void testResponse() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("TARGET", "service");
        final SamlService impl = SamlService.createServiceFrom(request, null);
        
        final Response response = impl.getResponse("ticketId");
        assertNotNull(response);
        assertEquals(ResponseType.REDIRECT, response.getResponseType());
        assertTrue(response.getUrl().contains("SAMLart="));
    }
    
    public void testResponseForJsession() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("TARGET", "http://www.cnn.com/;jsession=test");
        final SamlService impl = SamlService.createServiceFrom(request, null);
        
        assertEquals("http://www.cnn.com/", impl.getId());
    }
    
    public void testResponseWithNoTicket() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("TARGET", "service");
        final SamlService impl = SamlService.createServiceFrom(request, null);
        
        final Response response = impl.getResponse(null);
        assertNotNull(response);
        assertEquals(ResponseType.REDIRECT, response.getResponseType());
        assertFalse(response.getUrl().contains("SAMLart="));
    }
    
    public void testRequestBody() {
        final String body = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body><samlp:Request xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" MajorVersion=\"1\" MinorVersion=\"1\" RequestID=\"_192.168.16.51.1024506224022\" IssueInstant=\"2002-06-19T17:03:44.022Z\"><samlp:AssertionArtifact>artifact</samlp:AssertionArtifact></samlp:Request></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(body.getBytes());
        
        final SamlService impl = SamlService.createServiceFrom(request, null);
        assertEquals("artifact", impl.getArtifactId());
        assertEquals("_192.168.16.51.1024506224022", impl.getRequestID());
    }
}
