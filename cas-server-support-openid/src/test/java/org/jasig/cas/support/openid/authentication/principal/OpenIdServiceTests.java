/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.authentication.principal;

import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class OpenIdServiceTests extends TestCase {

    private OpenIdService openIdService;

    @Override
    protected void setUp() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("openid.identity", "http://openid.ja-sig.org/battags");
        request.addParameter("openid.return_to", "http://www.ja-sig.org/?service=fa");
        
        this.openIdService = OpenIdService.createServiceFrom(request);
    }
    
    public void testGetResponse() {
        final Response response = this.openIdService.getResponse("test");
        
        assertNotNull(response);
        
        assertEquals("test", response.getAttributes().get("openid.assoc_handle"));
        assertEquals("http://www.ja-sig.org/?service=fa", response.getAttributes().get("openid.return_to"));
        assertEquals("http://openid.ja-sig.org/battags", response.getAttributes().get("openid.identity"));
        
        final Response response2 = this.openIdService.getResponse(null);
        assertEquals("cancel", response2.getAttributes().get("openid.mode"));
    }
    
    public void testEquals() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("openid.identity", "http://openid.ja-sig.org/battags");
        request.addParameter("openid.return_to", "http://www.ja-sig.org/?service=fa");
        request.addParameter("openid.sig", this.openIdService.getSignature());
        
        final OpenIdService o = OpenIdService.createServiceFrom(request); 
        
        assertTrue(this.openIdService.equals(o));
        assertFalse(this.openIdService.equals(null));
        assertFalse(this.openIdService.equals(new Object()));
    }
}
