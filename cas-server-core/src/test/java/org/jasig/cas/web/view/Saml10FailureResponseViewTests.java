/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.util.Collections;

import org.jasig.cas.web.view.Cas10ResponseViewTests.MockWriterHttpMockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class Saml10FailureResponseViewTests extends TestCase {

    private Saml10FailureResponseView view = new Saml10FailureResponseView();
    
    public void testResponse() throws Exception {
        final MockHttpServletRequest request =  new MockHttpServletRequest();
        final MockWriterHttpMockHttpServletResponse response = new MockWriterHttpMockHttpServletResponse();
        request.addParameter("TARGET", "service");
        
        final String description = "Validation failed";
        this.view.renderMergedOutputModel(
            Collections.singletonMap("description", description), request, response);
        
        final String responseText = response.getWrittenValue();
        assertTrue(responseText.contains("Status"));
        assertTrue(responseText.contains(description));
    }
    
}
