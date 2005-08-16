/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class RedirectViewDescriptorCreatorTests extends TestCase {

    private static final String SERVICE = "service";
    
    private static final String TICKET = "ticket";
  
    private RedirectViewDescriptorCreator redirectViewDescriptorCreator;    

    protected void setUp() throws Exception {
        this.redirectViewDescriptorCreator = new RedirectViewDescriptorCreator();
    }

    public void testGetViewDescriptor() {
        final MockRequestContext context = new MockRequestContext();
        ContextUtils.addAttributeToFlowScope(context, WebConstants.SERVICE, SERVICE);
        ContextUtils.addAttributeToFlowScope(context, WebConstants.TICKET, TICKET);
        
        final ViewDescriptor viewDescriptor = this.redirectViewDescriptorCreator.createViewDescriptor(context);
        
        assertEquals(SERVICE, viewDescriptor.getViewName());
        assertTrue(viewDescriptor.getModel().containsValue(TICKET));
    } 
    
    public void testGetViewDescriptorNoTicket() {
        final MockRequestContext context = new MockRequestContext();
        ContextUtils.addAttributeToFlowScope(context, WebConstants.SERVICE, SERVICE);
        
        final ViewDescriptor viewDescriptor = this.redirectViewDescriptorCreator.createViewDescriptor(context);
        
        assertEquals(SERVICE, viewDescriptor.getViewName());
        assertFalse(viewDescriptor.getModel().containsValue(TICKET));
    } 
}
