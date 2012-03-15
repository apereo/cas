/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.jasig.cas.mock.MockService;
import org.junit.Test;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class RegisteredServiceImplTests {

	@Test
	public void testRegexServiceIds() {
		RegisteredServiceImpl svc = new RegisteredServiceImpl();
		svc.setId(1000);
		svc.setServiceId("http.?://\\w+\\.domain\\.\\w{3}$");

		assertTrue(svc.matches(new MockService("https://www.domain.edu")));
		assertTrue(svc.matches(new MockService("http://sub.domain.org")));

		assertTrue(svc.matches(new MockService("http://veryLongSubDomainWorksToo1234.domain.com")));
		assertFalse(svc.matches(new MockService("http://sub.domain.eu")));
		assertFalse(svc.matches(null));

		svc.setServiceId("\\w+://(\\w+\\.)*domain\\.\\w+");

		assertTrue(svc.matches(new MockService("https://www.domain.edu")));
		assertTrue(svc.matches(new MockService("http://anything.domain.education")));
		assertTrue(svc.matches(new MockService("imaps://domain.organization")));
		assertTrue(svc.matches(new MockService("whateverprotocol://test.domain.test")));
		assertTrue(svc.matches(new MockService("http://domain.eu")));
		assertTrue(svc.matches(new MockService("https://some.service.domain.edu?param1=value1")));
		assertTrue(svc.matches(new MockService("imap://my.service.domain.edu/first/second/third?hello=world")));

		svc.setServiceId("\\w+://(\\w+\\.)*mydomain.*");
		assertFalse(svc.matches(new MockService("http://pirate.domain?something.mydomain/")));
		assertFalse(svc.matches(new MockService("someStrangeProtocol://pirate.domain.edu/mydomain?myDomain=1")));
		assertTrue(svc.matches(new MockService("anything://myown.mydomain.anythingelse/first/second?param=value")));

		try {
			svc.setServiceId("*://www.invalid.edu");
			fail("Invalid service id is specified. Should always throw an exception");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
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
        
		RegisteredServiceImpl r = new RegisteredServiceImpl();

		r.setAllowedAttributes(ALLOWED_ATTRIBUTES);
		r.setAllowedToProxy(ALLOWED_TO_PROXY);
		r.setAnonymousAccess(ANONYMOUS_ACCESS);
		r.setDescription(DESCRIPTION);
		r.setEnabled(ENABLED);
		r.setId(ID);
		r.setName(NAME);
		r.setServiceId(SERVICEID);
		r.setSsoEnabled(SSO_ENABLED);
		r.setTheme(THEME);
        
		assertEquals(ALLOWED_ATTRIBUTES, r.getAllowedAttributes());
		assertEquals(ALLOWED_TO_PROXY, r.isAllowedToProxy());
		assertEquals(ANONYMOUS_ACCESS, r.isAnonymousAccess());
		assertEquals(DESCRIPTION, r.getDescription());
		assertEquals(ENABLED, r.isEnabled());
		assertEquals(ID, r.getId());
		assertEquals(NAME, r.getName());
		assertEquals(SERVICEID, r.getServiceId());
		assertEquals(SSO_ENABLED, r.isSsoEnabled());
		assertEquals(THEME, r.getTheme());
        
		assertFalse(r.equals(null));
		assertFalse(r.equals(new Object()));
		assertTrue(r.equals(r));
        
		r.setAllowedAttributes(null);
		assertNotNull(r.getAllowedAttributes());
    }
    
	@Test
    public void testEquals() {
        assertTrue(new RegisteredServiceImpl().equals(new RegisteredServiceImpl()));
        assertFalse(new RegisteredServiceImpl().equals(null));
        assertFalse(new RegisteredServiceImpl().equals(new Object()));
    }
}
