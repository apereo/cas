/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.remoteuser;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalBearingCredentials;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * Testcase for RemoteUserCredentialsBinder.
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class RemoteUserCredentialsBinderTest extends TestCase {

    RemoteUserCredentialsBinder binder = new RemoteUserCredentialsBinder();
    
    /**
     * Test that the binder extracts the remote user and binds it into the
     * PrincipalBearingCredentials.
     */
    public void testRemoteUser() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteUser("bayern");
        
        PrincipalBearingCredentials credentials = new PrincipalBearingCredentials();
        
        this.binder.bind(mockRequest, credentials);
        
        Principal principal = credentials.getPrincipal();
        assertNotNull(principal);
        assertEquals("bayern", principal.getId());
    }
    

    /**
     * Test that the binder does not place a Principal into the 
     * PrincipalBearingCredentials when remote user is null.
     */
    public void testNullRemoteUser() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        
        PrincipalBearingCredentials credentials = new PrincipalBearingCredentials();
        
        this.binder.bind(mockRequest, credentials);
        
        Principal principal = credentials.getPrincipal();
        assertNull(principal);
    }
    
}
