/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import net.sourceforge.jwebunit.HttpUnitDialog;

/**
 * Tests the /validate legacy (CAS 1.0) ticket validation service of a CAS server.
 * 
 * @author Scott Battaglia
 * @author Andrew Petro
 * @author Drew Mazurek
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ValidateCompatibilityTests extends AbstractCompatibilityTests {

	/**
	 * CAS 1.0 response indicating that the ticket was invalid.
	 */
	public static final String LEGACY_NO_RESPONSE = "no\n\n";
	
    public ValidateCompatibilityTests() throws IOException {
        super();
    }

    public ValidateCompatibilityTests(String name) throws IOException {
        super(name);
    }
    
    /**
     * Test /validate with no parameters set.
     */
    public void testNoParameters() {
        beginAt("/validate");
        assertTextPresent("no");
        
        // here we test that the response was exactly that specified 
        // in section 2.4.2 of the CAS spec
        HttpUnitDialog htDialog = getDialog();
        String validateOutput = htDialog.getResponseText();
        String exactExpectedResponse = LEGACY_NO_RESPONSE;
        
        assertEquals(exactExpectedResponse, validateOutput);
    }
    
    /**
     * Test that validating a bad service ticket results in the CAS 1 validation failure
     * response.
     * @throws UnsupportedEncodingException
     */
    public void testBadServiceTicket() throws UnsupportedEncodingException {
        final String service = "https://localhost:8443/contacts-cas/j_acegi_cas_security_check";
        beginAt("/validate?service=" + URLEncoder.encode(service, "UTF-8") + "&ticket=test");
        assertTextPresent("no");
        
        // here we test that the response was exactly that specified 
        // in section 2.4.2 of the CAS spec
        HttpUnitDialog htDialog = getDialog();
        String validateOutput = htDialog.getResponseText();
        
        assertEquals(LEGACY_NO_RESPONSE, validateOutput);
    }
    
    /**
     * Test for the correct CAS1 validation success response.
     * @throws IOException
     */
    public void testProperCredentialsAndServiceTicket() throws IOException {
    	
    	// log into CAS and obtain a service ticket
    	
        final String service = "http://www.cnn.com";
        beginAt("/login?service=" + URLEncoder.encode(service, "UTF-8"));
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();
        
        HttpUnitDialog htDialog = getDialog();
        String response = htDialog.getResponse().getText();
        
        // the serviceTicket, which we'll find in one of two ways
        String serviceTicket = null;
        
        // now we need to extract the service ticket.
        
        // in a baseline CAS 2.x distribution return to the service is accomplished by 
        // JavaScript redirect
        // 
        // CAS 3 accomplishes this differently such that our client has already
        // followed the redirect to the service, so we'll find the service ticket
        // on the response URL.
        
        String queryString = htDialog.getResponse().getURL().getQuery();
        
        int ticketIndex = queryString.indexOf("ticket=");
        
        if (ticketIndex == -1) {

        	// the ticket wasn't in the response URL.
            // we're testing for CAS 2.x style JavaScript for redirection, as 
        	// recommended in appendix B of the CAS 2 protocol specification
        	
        	// parse the ticket out of the JavaScript
            
            int declarationStartsAt = response.indexOf("window.location.href");
            // cut off the front of the response up to the beginning of the service URL
            String responseAfterWindowLocHref = response.substring(declarationStartsAt + "window.location.href12".length());
            
            // The URL might be single or double quoted
            final int endDoubleQuoteIndex = responseAfterWindowLocHref.indexOf("\"");
            final int endSingleQuoteIndex = responseAfterWindowLocHref.indexOf("\'");
            
            // we will set this variable to be the index of the first ' or " character
            int endQuoteIndex = 0;
            if (endDoubleQuoteIndex == -1 && endSingleQuoteIndex == -1) {
            	fail("Failed parsing a service ticket from the response:" + response);
            } else if ( (endDoubleQuoteIndex > -1) && 
            		(endDoubleQuoteIndex < endSingleQuoteIndex || endSingleQuoteIndex == -1)) {
            	endQuoteIndex = endDoubleQuoteIndex;
            } else {
            	endQuoteIndex = endSingleQuoteIndex;
            }
            
            int ticketEqualsIndex = responseAfterWindowLocHref.indexOf("ticket=");
            
            serviceTicket = responseAfterWindowLocHref.substring(ticketEqualsIndex + "ticket=".length(), endQuoteIndex);

        	
        } else {
        	// service ticket was found on query String, parse it from there
        	
        	// TODO Is this type of redirection compatible?  
        	// Does it address all the issues that CAS2 JavaScript redirection
        	// was intended to address?
        	
        	serviceTicket = queryString.substring(ticketIndex + "ticket=".length(), queryString.length());
        	
        }
        
        beginAt("/validate?service=" + URLEncoder.encode(service, "UTF-8") + "&ticket=" + serviceTicket);
        assertTextPresent("yes");
        
        // here we test that the response was exactly that specified 
        // in section 2.4.2 of the CAS spec
        htDialog = getDialog();
        String validateOutput = htDialog.getResponseText();
        
        String expected = "yes\n" + getUsername() + "\n";
        
        assertEquals(expected, validateOutput);

    }
}
