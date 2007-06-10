/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public class AutomaticCookiePathSetterActionTests extends TestCase {
    private final AutomaticCookiePathSetterAction action = new AutomaticCookiePathSetterAction();
    
    private CookieGenerator warnCookieGenerator;
    
    private CookieGenerator tgtCookieGenerator;

    protected void setUp() throws Exception {
        this.warnCookieGenerator = new CookieGenerator();
        this.tgtCookieGenerator = new CookieGenerator();
        this.action.setTicketGrantingTicketCookieGenerator(this.tgtCookieGenerator);
        this.action.setWarnCookieGenerator(this.warnCookieGenerator);
        this.action.afterPropertiesSet();
    }
    
    public void testSettingContextPath() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String CONST_CONTEXT_PATH = "/test";
        request.setContextPath(CONST_CONTEXT_PATH);
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        
        this.action.doExecute(context);
        
        assertEquals(CONST_CONTEXT_PATH, this.warnCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH, this.tgtCookieGenerator.getCookiePath());
    }
    
    public void testResettingContexPath() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String CONST_CONTEXT_PATH = "/test";
        final String CONST_CONTEXT_PATH_2 = "/test1";
        request.setContextPath(CONST_CONTEXT_PATH);
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        
        this.action.doExecute(context);
        
        assertEquals(CONST_CONTEXT_PATH, this.warnCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH, this.tgtCookieGenerator.getCookiePath());
        
        request.setContextPath(CONST_CONTEXT_PATH_2);
        this.action.doExecute(context);
        
        assertNotSame(CONST_CONTEXT_PATH_2, this.warnCookieGenerator.getCookiePath());
        assertNotSame(CONST_CONTEXT_PATH_2, this.tgtCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH, this.warnCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH, this.tgtCookieGenerator.getCookiePath());
    }
}
