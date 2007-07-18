/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Service;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class TicketValidationExceptionTests extends TestCase {

    private static final String CODE = "INVALID_SERVICE";
    
    private Service service = TestUtils.getService();
    
    
    public void testThrowableConstructor() {
        final TicketValidationException t = new TicketValidationException(this.service);
        
        assertSame(CODE, t.getCode());
        assertEquals(this.service, t.getOriginalService());
    }
}
