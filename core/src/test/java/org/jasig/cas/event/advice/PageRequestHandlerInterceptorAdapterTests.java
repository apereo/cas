/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import org.jasig.cas.event.HttpRequestEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class PageRequestHandlerInterceptorAdapterTests extends TestCase {
    private HttpRequestHandlerInterceptorAdapter adapter = new HttpRequestHandlerInterceptorAdapter();
    private ApplicationEventPublisher eventPublisher = new MockApplicationEventPublisher();
    HttpRequestEvent event = null;
    
    public PageRequestHandlerInterceptorAdapterTests() {
        this.adapter.setApplicationEventPublisher(this.eventPublisher);
    }
    
    public void testPublishEvent() throws Exception {
        this.adapter.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), null, null);
        assertNotNull(this.event);
    }
    
    protected class MockApplicationEventPublisher implements ApplicationEventPublisher {

        public void publishEvent(ApplicationEvent arg0) {
            PageRequestHandlerInterceptorAdapterTests.this.event = (HttpRequestEvent) arg0;
        }
    }
}
