/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class TicketExceptionTests extends TestCase {
    public void testTicketExceptionToString() {
        final String CODE = "CODE";
        TicketException.TicketExceptionCode ticketExceptionCode = new TicketException.TicketExceptionCode(CODE);
        assertEquals(CODE, ticketExceptionCode.toString());
    }
    
    public void testNoParamConstructor() {
        new TicketException();
    }
    
    public void testCodeDescriptionConstructor() {
        final String DESCRIPTION = "test";
        TicketException t = new TicketException(TicketException.INVALID_SERVICE, DESCRIPTION);
        
        assertEquals(TicketException.INVALID_SERVICE.toString(), t.getCode());
        assertEquals(DESCRIPTION, t.getDescription());
    }
    
    public void testMessageCodeDescriptionConstructor() {
        final String DESCRIPTION = "test";
        final String MESSAGE = "test1";
        
        TicketException t = new TicketException(MESSAGE, TicketException.INVALID_SERVICE, DESCRIPTION);
        
        assertEquals(MESSAGE, t.getMessage());
        assertEquals(TicketException.INVALID_SERVICE.toString(), t.getCode());
        assertEquals(DESCRIPTION, t.getDescription());
    }
    
    public void testMessageCauseCodeDescriptionConstructor() {
        final String DESCRIPTION = "test";
        final String MESSAGE = "test1";
        final Throwable THROWABLE = new Throwable();
        
        TicketException t = new TicketException(MESSAGE, THROWABLE, TicketException.INVALID_SERVICE, DESCRIPTION);
        
        assertEquals(MESSAGE, t.getMessage());
        assertEquals(TicketException.INVALID_SERVICE.toString(), t.getCode());
        assertEquals(DESCRIPTION, t.getDescription());
        assertEquals(THROWABLE, t.getCause());
    }

    public void testCauseCodeDescriptionConstructor() {
        final String DESCRIPTION = "test";
        final Throwable THROWABLE = new Throwable();
        
        TicketException t = new TicketException(THROWABLE, TicketException.INVALID_SERVICE, DESCRIPTION);
        
        assertEquals(TicketException.INVALID_SERVICE.toString(), t.getCode());
        assertEquals(DESCRIPTION, t.getDescription());
        assertEquals(THROWABLE, t.getCause());
    }
}
