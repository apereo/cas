/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class TicketCreationExceptionTests extends TestCase {

    public void testNoParamConstructor() {
        new TicketCreationException();
    }

    public void testThrowableParamConstructor() {
        final Throwable THROWABLE = new Throwable();
        TicketCreationException t = new TicketCreationException(THROWABLE);

        assertEquals(THROWABLE, t.getCause());
    }
}
