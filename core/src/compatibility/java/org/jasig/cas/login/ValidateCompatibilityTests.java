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
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class ValidateCompatibilityTests extends AbstractCompatibilityTests {

    public ValidateCompatibilityTests() throws IOException {
        super();
    }

    public ValidateCompatibilityTests(String name) throws IOException {
        super(name);
    }
    
    public void testNoParameters() {
        beginAt("/validate");
        assertTextPresent("no");
    }
    
    public void testBadServiceTicket() throws UnsupportedEncodingException {
        final String service = "https://localhost:8443/contacts-cas/j_acegi_cas_security_check";
        beginAt("/validate?service=" + URLEncoder.encode(service, "UTF-8") + "&ticket=test");
        assertTextPresent("no");
    }
    
    public void testProperCredentialsAndServiceTicket() throws UnsupportedEncodingException {
        final String service = "http://www.cnn.com";
        beginAt("/login?service=" + URLEncoder.encode(service, "UTF-8"));
        setFormElement("username", "test");
        setFormElement("password", "test");
        submit();
        
        // TODO testProperCredentialsAndServiceTicket
        
        assertTextPresent("test");

    }
}
