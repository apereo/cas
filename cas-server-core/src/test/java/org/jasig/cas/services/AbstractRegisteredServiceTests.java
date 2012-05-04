/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Service;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for {@link AbstractRegisteredService}.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class AbstractRegisteredServiceTests {

    private AbstractRegisteredService r = new AbstractRegisteredService() {
        public void setServiceId(final String id) {
            serviceId = id;
        }

        protected AbstractRegisteredService newInstance() {
            return this;
        }

        public boolean matches(final Service service) {
            return true;
        }
    };

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

    @Test
    public void testEquals() throws Exception {
        assertTrue(r.equals(r.clone()));
        assertFalse(new RegisteredServiceImpl().equals(null));
        assertFalse(new RegisteredServiceImpl().equals(new Object()));
    }
}
