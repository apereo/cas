/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

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

	public void testServiceEvaluationOrder() {
		RegisteredServiceImpl sv1 = new RegisteredServiceImpl();
		sv1.setServiceId("http://**");

		RegisteredServiceImpl sv2 = new RegisteredServiceImpl();
		sv2.setServiceId("https://**");

		RegisteredServiceImpl sv3 = new RegisteredServiceImpl();
		sv3.setServiceId("imaps://**");

		RegisteredServiceImpl sv4 = new RegisteredServiceImpl();
		sv4.setServiceId("imap://**");

		RegisteredServiceImpl sv5 = new RegisteredServiceImpl();
		sv5.setServiceId("http://com/?test.jsp");

		RegisteredServiceImpl sv6 = new RegisteredServiceImpl();
		sv6.setServiceId("http://*.jsp");

		RegisteredServiceImpl sv7 = new RegisteredServiceImpl();
		sv7.setServiceId("com/**/test.jsp");

		RegisteredServiceImpl sv8 = new RegisteredServiceImpl();
		sv8.setServiceId("com/**/test?ng.jsp");

		RegisteredServiceImpl sv9 = new RegisteredServiceImpl();
		sv9.setServiceId("com/**/????.js*");

		RegisteredServiceImpl sv10 = new RegisteredServiceImpl();
		sv10.setServiceId("http://www.service.edu");

		RegisteredServiceImpl sv11 = new RegisteredServiceImpl();
		sv11.setServiceId("http://www.service.edu/some/test/page/test.jsp");

		RegisteredServiceImpl sv12 = new RegisteredServiceImpl();
		sv12.setServiceId("http://www.service.c?m/**");

		RegisteredServiceImpl sv13 = new RegisteredServiceImpl();
		sv13.setServiceId("http://www.service.com/test/page/**");

		RegisteredServiceImpl sv14 = new RegisteredServiceImpl();
		sv14.setServiceId("http://www.service.edu/some/test/page/test.asp?");

		RegisteredServiceImpl sv15 = new RegisteredServiceImpl();
		sv15.setServiceId("http?://**");

		RegisteredServiceImpl sv16 = new RegisteredServiceImpl();
		sv16.setServiceId("http://www.college.edu");

		assertTrue(sv1.compareTo(sv1) == 0);
		assertTrue(sv1.compareTo(sv2) > 0);
		assertTrue(sv3.compareTo(sv4) < 0);
		assertTrue(sv1.compareTo(sv4) == 0);
		assertTrue(sv5.compareTo(sv6) < 0);
		assertTrue(sv7.compareTo(sv6) > 0);
		assertTrue(sv8.compareTo(sv6) > 0);
		assertTrue(sv9.compareTo(sv8) > 0);
		assertTrue(sv10.compareTo(sv11) > 0);
		assertTrue(sv12.compareTo(sv10) > 0);
		assertTrue(sv13.compareTo(sv12) < 0);
		assertTrue(sv13.compareTo(sv14) > 0);
		assertTrue(sv14.compareTo(sv11) < 0);
		assertTrue(sv16.compareTo(sv10) == 0);
	}

    public void testEquals() {
        assertTrue(new RegisteredServiceImpl().equals(new RegisteredServiceImpl()));
        assertFalse(new RegisteredServiceImpl().equals(null));
        assertFalse(new RegisteredServiceImpl().equals(new Object()));
    }
}
