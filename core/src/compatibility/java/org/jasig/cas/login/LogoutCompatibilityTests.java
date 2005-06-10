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
    
    /**
     * Test that the logout UI follows the recommended behavior of painting
     * a link to the URL specified by an application redirecting for logout.
     * 
     * CAS servers failing this test are not necessarily CAS2 non-compliant, as
     * support for this behavior is recommended but not required.
     * @throws UnsupportedEncodingException
     */
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
