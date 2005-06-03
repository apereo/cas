/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import junit.framework.TestCase;

import org.jasig.cas.ticket.registry.DefaultTicketRegistry;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class CentralAuthenticationServiceAfterReturningAdviceTests extends TestCase {
    private CentralAuthenticationServiceAfterReturningAdvice advice = new CentralAuthenticationServiceAfterReturningAdvice();
    
    public void testAfterPropertiesSet() throws Exception {
        this.advice.setTicketRegistry(new DefaultTicketRegistry());
        this.advice.afterPropertiesSet();
    }
}
