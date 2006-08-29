/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.event.advice;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadUsernameOrPasswordAuthenticationException;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
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
public class AuthenticationHandlerMethodInterceptorTests extends TestCase {

    private AuthenticationHandlerMethodInterceptor advice = new AuthenticationHandlerMethodInterceptor();

    AuthenticationEvent event = null;

    protected void setUp() throws Exception {
        this.event = null;
        this.advice
            .setApplicationEventPublisher(new MockApplicationEventPublisher());
    }

    public void testAuthenticationEventWithBooleanTrue() throws Throwable {
        this.advice.invoke(new BooleanMethodInvocation(Boolean.TRUE));
        assertNotNull(this.event);
        assertTrue(this.event.isSuccessfulAuthentication());
    }

    public void testAuthenticationEventWithBooleanFalse() throws Throwable {
        this.advice.invoke(new BooleanMethodInvocation(Boolean.FALSE));
        assertNotNull(this.event);
        assertFalse(this.event.isSuccessfulAuthentication());
    }

    public void testAuthenticationEventWithException() throws Throwable {
        try {
            this.advice.invoke(new BooleanMethodInvocation(
                BadUsernameOrPasswordAuthenticationException.ERROR));
        } catch (AuthenticationException e) {
            // ok
        }
        assertNotNull(this.event);
        assertFalse(this.event.isSuccessfulAuthentication());
    }

    protected class BooleanMethodInvocation implements MethodInvocation {

        private final Object returnObject;

        protected BooleanMethodInvocation(final Object returnObject) {
            this.returnObject = returnObject;
        }

        public Method getMethod() {
            return SimpleTestUsernamePasswordAuthenticationHandler.class
                .getDeclaredMethods()[0];
        }

        public Object[] getArguments() {
            return new Object[] {new UsernamePasswordCredentials()};
        }

        public AccessibleObject getStaticPart() {
            return null;
        }

        public Object getThis() {
            return null;
        }

        public Object proceed() throws Throwable {
            if (this.returnObject instanceof Throwable) {
                throw (Throwable) this.returnObject;
            }
            return this.returnObject;
        }
    }

    protected class MockApplicationEventPublisher implements
        ApplicationEventPublisher {

        public void publishEvent(ApplicationEvent arg0) {
            AuthenticationHandlerMethodInterceptorTests.this.event = (AuthenticationEvent) arg0;
        }
    }
}
