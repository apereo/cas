/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import java.lang.reflect.Method;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.event.AuthenticationEvent;
import org.springframework.aop.AfterReturningAdvice;
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
public final class AuthenticationHandlerAfterReturningAdvice implements
    AfterReturningAdvice, ApplicationEventPublisherAware {

    /** The publisher to publish events. */
    private ApplicationEventPublisher applicationEventPublisher;

    public void setApplicationEventPublisher(
        final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void afterReturning(final Object returnValue, final Method method,
        final Object[] args, final Object arg3) throws Throwable {

        final Boolean value = (Boolean) returnValue;
        this.applicationEventPublisher.publishEvent(new AuthenticationEvent(
            (Credentials) args[0], value.booleanValue()));
    }
}
