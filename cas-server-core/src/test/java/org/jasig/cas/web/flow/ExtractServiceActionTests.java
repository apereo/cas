/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.util.Arrays;

import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.CasArgumentExtractor;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class ExtractServiceActionTests extends TestCase {
    
    private ExtractServiceAction action;

    protected void setUp() throws Exception {
        final ArgumentExtractor[] argExtractors = new ArgumentExtractor[] {new CasArgumentExtractor()};        
        this.action = new ExtractServiceAction();
        this.action.setArgumentExtractors(Arrays.asList(argExtractors));
        this.action.afterPropertiesSet();
        super.setUp();
    }
    
    public void testNoServiceFound() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        
        final Event event = this.action.execute(context);

        assertNull(WebUtils.getService(context));
        
        assertEquals("success", event.getId());
    }
    
    public void testServiceFound() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        
        final Event event = this.action.execute(context);

        assertEquals("test", WebUtils.getService(context).getId());
        assertEquals("success", event.getId());
    }

}
