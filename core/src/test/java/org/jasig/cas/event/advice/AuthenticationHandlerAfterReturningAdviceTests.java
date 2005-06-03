/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.event.AuthenticationEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AuthenticationHandlerAfterReturningAdviceTests extends TestCase {

    private AuthenticationHandlerAfterReturningAdvice advice = new AuthenticationHandlerAfterReturningAdvice();

    AuthenticationEvent event = null;

    protected void setUp() throws Exception {
        this.event = null;
        this.advice.setApplicationEventPublisher(new MockApplicationEventPublisher());
    }
    
    public void testAuthenticationEventWithBooleanTrue() throws Throwable {
        Boolean value = Boolean.TRUE;
        this.advice.afterReturning(value, AuthenticationHandler.class.getDeclaredMethods()[0], new Object[] {new UsernamePasswordCredentials()}, null);
        assertNotNull(this.event);
        assertTrue(this.event.isSuccessfulAuthentication());
    }
    
    public void testAuthenticationEventWithBooleanFalse() throws Throwable {
        Boolean value = Boolean.FALSE;
        this.advice.afterReturning(value, AuthenticationHandler.class.getDeclaredMethods()[0], new Object[] {new UsernamePasswordCredentials()}, null);
        assertNotNull(this.event);
        assertFalse(this.event.isSuccessfulAuthentication());
    }

    protected class MockApplicationEventPublisher implements ApplicationEventPublisher {

        public void publishEvent(ApplicationEvent arg0) {
            AuthenticationHandlerAfterReturningAdviceTests.this.event = (AuthenticationEvent) arg0;
        }
    }
}
