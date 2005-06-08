/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.event.AuthenticationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Advice to advise an AuthenticationHandler such that it checks the return
 * value returned by the authenticate method and constructs an
 * AuthenticationEvent that captures the result and publishes it for an
 * EventHandler to process.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * @see org.jasig.cas.event.AuthenticationEvent
 */
public final class AuthenticationHandlerMethodInterceptor implements
    MethodInterceptor, ApplicationEventPublisherAware {

    /** The publisher to publish events. */
    private ApplicationEventPublisher applicationEventPublisher;

    public void setApplicationEventPublisher(
        final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public Object invoke(final MethodInvocation methodInvocation)
        throws Throwable {

        try {
            final Boolean returnValue = (Boolean) methodInvocation.proceed();
            this.applicationEventPublisher
                .publishEvent(new AuthenticationEvent(
                    (Credentials) methodInvocation.getArguments()[0],
                    returnValue.booleanValue(), methodInvocation.getMethod()
                        .getDeclaringClass()));
            return returnValue;
        } catch (AuthenticationException e) {
            this.applicationEventPublisher
                .publishEvent(new AuthenticationEvent(
                    (Credentials) methodInvocation.getArguments()[0], false,
                    methodInvocation.getMethod().getDeclaringClass()));
            throw e;
        }
    }
}
