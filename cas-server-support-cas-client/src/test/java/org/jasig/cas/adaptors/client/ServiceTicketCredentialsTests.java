/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.client;

import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class ServiceTicketCredentialsTests extends TestCase {

    private static final String CONST_TICKET_ID = "ticket";

    private ServiceTicketCredentials c;

    protected void setUp() throws Exception {
        this.c = new ServiceTicketCredentials(CONST_TICKET_ID);
    }

    public void testGetterForServiceTicket() {
        assertEquals(CONST_TICKET_ID, this.c.getServiceTicketId());
    }

    public void testAssertions() {
        final Assertion assertion = new AssertionImpl(new SimplePrincipal(
            "test"));
        this.c.setAssertion(assertion);

        assertEquals(assertion, this.c.getAssertion());
    }
}
