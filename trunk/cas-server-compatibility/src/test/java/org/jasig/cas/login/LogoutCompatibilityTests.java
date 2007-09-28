/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
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

    
    /**
     * Test that after logout SSO doesn't happen - visiting login
     * leads to the login screen.  Also test that logout renders a previous
     * service ticket invalid.
     * @throws IOException
     */
    public void testLogoutEndsSso() throws IOException {
    	// demonstrate lack of SSO session
    	String serviceUrl = getServiceUrl();
    	String encodedService = URLEncoder.encode(serviceUrl, "UTF-8");
    	beginAt("/login?service=" + encodedService);
    	
    	// verify that login screen is painted
    	assertFormElementPresent(LOGIN_TOKEN);
    	
    	// establish SSO session
    	
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();
    	
        String firstServiceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());
        assertNotNull(firstServiceTicket);
        
        // demonstate successful validation of st before logout
        
        beginAt("/serviceValidate?service=" + encodedService + "&ticket=" + firstServiceTicket);
        assertTextPresent("<cas:authenticationSuccess");
        
    	// demonstrate SSO session
    	
        beginAt("/login?service=" + encodedService);
        
        String secondServiceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());
        assertNotNull(secondServiceTicket);
        assertFalse(firstServiceTicket.equals(secondServiceTicket));
        
    	// log out
    	
        beginAt("/logout");
        
    	// demonstrate lack of SSO session
        
        beginAt("/login?service=" + encodedService);
        assertFormElementPresent(LOGIN_TOKEN);
        
        // demonstate that the second service ticket no longer validates
        
        beginAt("/serviceValidate?service=" + encodedService + "&ticket=" + secondServiceTicket);
        assertTextPresent("<cas:authenticationFailure");
        
    }
    
}
