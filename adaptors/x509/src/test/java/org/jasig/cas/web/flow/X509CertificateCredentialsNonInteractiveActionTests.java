/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.principal.AbstractX509CertificateTests;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.servlet.ServletEvent;
import org.springframework.webflow.test.MockRequestContext;


public class X509CertificateCredentialsNonInteractiveActionTests extends
    AbstractX509CertificateTests {
    
    private X509CertificateCredentialsNonInteractiveAction action;

    protected void setUp() throws Exception {
        this.action = new X509CertificateCredentialsNonInteractiveAction();
    }
    
    public void testNoCredentialsResultsInError() {
        final MockRequestContext context = new MockRequestContext();
        context.setSourceEvent(new ServletEvent(new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertEquals("error", this.action.doExecuteInternal(context, "test", "test", false, false, false).getId());
    }
    
    

}
