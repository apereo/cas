/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.event.HttpRequestEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * HandlerInterceptor that will on a successful completion of a page rendering,
 * publish a PageRequestHttpRequestEvent. PageRequestHttpRequestEvents
 * encapsulate the HttpServletRequest object in order to allow EventHandlerse to
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * @see org.jasig.cas.event.HttpRequestEvent
 */
public final class HttpRequestHandlerInterceptorAdapter extends
    HandlerInterceptorAdapter implements HandlerInterceptor,
    ApplicationEventPublisherAware {

    /** The publisher to publish events. */
    private ApplicationEventPublisher applicationEventPublisher;

    public void setApplicationEventPublisher(
        final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void afterCompletion(final HttpServletRequest request,
        final HttpServletResponse response, final Object handler,
        final Exception ex) throws Exception {
        final HttpRequestEvent event = new HttpRequestEvent(request);
        this.applicationEventPublisher.publishEvent(event);
    }
}
