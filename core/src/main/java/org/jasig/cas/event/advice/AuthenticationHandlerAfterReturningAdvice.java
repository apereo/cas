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
 * AfterReturningAdvice to publish events about any AuthenticationHandler
 * authenticating a request.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * 
 * @see org.jasig.cas.AuthenticationAvent
 *
 */
public class AuthenticationHandlerAfterReturningAdvice implements
    AfterReturningAdvice, ApplicationEventPublisherAware {

    /** The publisher to publish events. */
    private ApplicationEventPublisher applicationEventPublisher;

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void afterReturning(Object returnValue, Method method, Object[] args,
        Object arg3) throws Throwable {
        
        final Boolean value = (Boolean) returnValue;
        this.applicationEventPublisher.publishEvent(new AuthenticationEvent((Credentials) args[0], value.booleanValue()));
    }
}
