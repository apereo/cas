/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
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
public class InvalidTicketClassExceptionTests extends TestCase {

      public void testNoParamConstructor() {
          new InvalidTicketClassException();
      }
      
      public void testMessageParamConstructor() {
          final String MESSAGE = "test";
          InvalidTicketClassException t = new InvalidTicketClassException(MESSAGE);
          assertEquals(MESSAGE, t.getMessage());
      }
      
      public void testMessageAndThrowableParamsConstructor() {
          final String MESSAGE = "test";
          final Throwable THROWABLE = new Throwable();
          InvalidTicketClassException t = new InvalidTicketClassException(MESSAGE, THROWABLE);
          
          assertEquals(MESSAGE, t.getMessage());
          assertEquals(THROWABLE, t.getCause());
      }
      
      public void testThrowableParamConstructor() {
          final Throwable THROWABLE = new Throwable();
          InvalidTicketClassException t = new InvalidTicketClassException(THROWABLE);
          
          assertEquals(THROWABLE, t.getCause());
      }
      
}
