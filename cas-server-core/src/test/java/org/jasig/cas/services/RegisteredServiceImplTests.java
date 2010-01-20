/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class RegisteredServiceImplTests extends TestCase {

    private RegisteredServiceImpl r = new RegisteredServiceImpl();
    
    public void testSettersAndGetters() {
        final long ID = 1000;
        final String DESCRIPTION = "test";
        final String SERVICEID = "serviceId";
        final String THEME = "theme";
        final String NAME = "name";
        final boolean ENABLED = false;
        final boolean ALLOWED_TO_PROXY = false;
        final boolean ANONYMOUS_ACCESS = true;
        final boolean SSO_ENABLED = false;
        final List<String> ALLOWED_ATTRIBUTES = Arrays.asList("Test");
        
        this.r.setAllowedAttributes(ALLOWED_ATTRIBUTES);
        this.r.setAllowedToProxy(ALLOWED_TO_PROXY);
        this.r.setAnonymousAccess(ANONYMOUS_ACCESS);
        this.r.setDescription(DESCRIPTION);
        this.r.setEnabled(ENABLED);
        this.r.setId(ID);
        this.r.setName(NAME);
        this.r.setServiceId(SERVICEID);
        this.r.setSsoEnabled(SSO_ENABLED);
        this.r.setTheme(THEME);
        
        assertEquals(ALLOWED_ATTRIBUTES, this.r.getAllowedAttributes());
        assertEquals(ALLOWED_TO_PROXY, this.r.isAllowedToProxy());
        assertEquals(ANONYMOUS_ACCESS, this.r.isAnonymousAccess());
        assertEquals(DESCRIPTION, this.r.getDescription());
        assertEquals(ENABLED, this.r.isEnabled());
        assertEquals(ID, this.r.getId());
        assertEquals(NAME, this.r.getName());
        assertEquals(SERVICEID, this.r.getServiceId());
        assertEquals(SSO_ENABLED, this.r.isSsoEnabled());
        assertEquals(THEME, this.r.getTheme());
        
        assertFalse(this.r.equals(null));
        assertFalse(this.r.equals(new Object()));
        assertTrue(this.r.equals(this.r));
        
        this.r.setAllowedAttributes(null);
        assertNotNull(this.r.getAllowedAttributes());
    }
    
    public void testEquals() {
        assertTrue(new RegisteredServiceImpl().equals(new RegisteredServiceImpl()));
        assertFalse(new RegisteredServiceImpl().equals(null));
        assertFalse(new RegisteredServiceImpl().equals(new Object()));
    }
}
