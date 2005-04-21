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
public class LogoutCompatibilityTests extends AbstractCompatibilityTests {

    public LogoutCompatibilityTests() throws IOException {
        super();
    }
    
    public LogoutCompatibilityTests(final String name) throws IOException {
        super(name);
    }
    
    public void testUrlParameter() throws UnsupportedEncodingException {
        final String service = "https://localhost:8443/contacts-cas/j_acegi_cas_security_check";
        beginAt("/logout?url=" + URLEncoder.encode(service, "UTF-8"));

        assertTextPresent(service);
    }
    
    public void testShowLoggedOutPage() {
        beginAt("/logout");

        assertTextPresent("logged out");
    }
}
