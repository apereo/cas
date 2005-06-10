/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Common testcases for /serviceValidate and /proxyValidate.
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class Cas2ValidateCompatibilityTests extends AbstractCompatibilityTests {

    public Cas2ValidateCompatibilityTests() throws IOException {
        super();
    }

    public Cas2ValidateCompatibilityTests(String name) throws IOException {
        super(name);
    }
    
    /**
     * Returns /serviceValidate in the case of /serviceValidate, 
     * and /proxyValidate in the case of /proxyValidate.
     * Concrete subclasses implement this method to configure the common
     * tests defined here.
     * @return
     */
    protected abstract String getValidationPath();
    
    public void testNoParameters() {
        beginAt(getValidationPath());
        assertTextPresent("cas:authenticationFailure");
        
        // TODO: actually test the validation response XML.
    }
    
    public void testBadServiceTicket() throws UnsupportedEncodingException {
        final String service = "https://localhost:8443/contacts-cas/j_acegi_cas_security_check";
        beginAt(getValidationPath() + "?service=" + URLEncoder.encode(service, "UTF-8") + "&ticket=test");
        
        assertTextPresent("cas:authenticationFailure");
        
        // TODO: do more to test that the response is actually XML, etc. etc.
    }
    
    /**
     * Test validation of a valid service ticket and that service tickets are
     * not multiply validatable.
     * @throws IOException
     */
    public void testProperCredentialsAndServiceTicket() throws IOException {
        final String service = "https://localhost:18443/compat-test-support/displayTicket.jsp";
        String encodedService = URLEncoder.encode(service, "UTF-8");
        beginAt("/login?service=" + encodedService);
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();
        
        // read the service ticket
        
        String serviceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());
        
        // great, now we have a ticket
        
        // let's validate it
        
        beginAt(getValidationPath() + "?service=" + encodedService + "&" + "ticket=" + serviceTicket);
        
        assertTextPresent("cas:authenticationSuccess");
        
        // this assertion may be too strict.  How does whitespace work here?
        assertTextPresent("<cas:user>" + getUsername() + "</cas:user>");
        
        // TODO: do more to test that the response is actually XML, etc. etc.
        
        // let's validate it again and ensure that we cannot again validate
        // the ticket
        
        beginAt(getValidationPath() + "?service=" + encodedService + "&" + "ticket=" + serviceTicket);
        assertTextPresent("cas:authenticationFailure");
        
        // TODO: do more to test that the response is actually XML, etc. etc.
        
    }
}
