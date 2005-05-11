/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import junit.framework.TestCase;


public class RedirectControllerTests extends TestCase {

    private RedirectController redirectController = new RedirectController();
    
    public void testUrlAndModel() throws Exception {
        Map model = new HashMap();
        model.put("Test", "test");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("service", "test");
        request.setAttribute("model", model);
        
        ModelAndView modelAndView = this.redirectController.handleRequest(request, new MockHttpServletResponse());
        
        assertEquals(modelAndView.getModel(), model);
        assertEquals("test", ((RedirectView) modelAndView.getView()).getUrl());
    }
    
    public void testUrlAndNoModel() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("service", "test");
        
        ModelAndView modelAndView = this.redirectController.handleRequest(request, new MockHttpServletResponse());
        
        assertTrue(modelAndView.getModel().isEmpty());
        assertEquals("test", ((RedirectView) modelAndView.getView()).getUrl());
    }
}
