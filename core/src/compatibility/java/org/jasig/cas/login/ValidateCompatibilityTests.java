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
        
        String serviceTicket = LoginHelper.serviceTicketFromResponse(htDialog.getResponse());
        
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
