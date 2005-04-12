/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.support.StaticWebApplicationContext;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class PageRequestHandlerInterceptorAdapterTests extends TestCase {
    private PageRequestHandlerInterceptorAdapter adapter = new PageRequestHandlerInterceptorAdapter();
    private StaticWebApplicationContext context = new StaticWebApplicationContext();
    
    public PageRequestHandlerInterceptorAdapterTests() {
        this.adapter.setApplicationEventPublisher(this.context);
        this.context.refresh();
    }
    
    public void testPublishEvent() throws Exception {
        this.adapter.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), null, null);
    }
}
