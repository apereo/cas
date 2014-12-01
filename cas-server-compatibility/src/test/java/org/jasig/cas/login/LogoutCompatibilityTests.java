/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author Scott Battaglia
 * @since 3.0.0
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
    public void verifyUrlParameter() throws UnsupportedEncodingException {
        final String service = "https://localhost:8443/contacts-cas/j_acegi_cas_security_check";
        beginAt("/logout?url=" + URLEncoder.encode(service, "UTF-8"));

        assertTextPresent(service);
    }

    public void verifyShowLoggedOutPage() {
        beginAt("/logout");

        assertTextPresent("logged out");
    }


    /**
     * Test that after logout SSO doesn't happen - visiting login
     * leads to the login screen.  Also test that logout renders a previous
     * service ticket invalid.
     * @throws IOException
     */
    public void verifyLogoutEndsSso() throws IOException {
        // demonstrate lack of SSO session
        final String serviceUrl = getServiceUrl();
        final String encodedService = URLEncoder.encode(serviceUrl, "UTF-8");
        beginAt("/login?service=" + encodedService);

        // verify that login screen is painted
        assertFormElementPresent(LOGIN_TOKEN);

        // establish SSO session

        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        final String firstServiceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());
        assertNotNull(firstServiceTicket);

        // Demonstrate successful validation of st before logout

        beginAt("/serviceValidate?service=" + encodedService + "&ticket=" + firstServiceTicket);
        assertTextPresent("<cas:authenticationSuccess");

        // demonstrate SSO session

        beginAt("/login?service=" + encodedService);

        final String secondServiceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());
        assertNotNull(secondServiceTicket);
        assertFalse(firstServiceTicket.equals(secondServiceTicket));

        // log out

        beginAt("/logout");

        // demonstrate lack of SSO session

        beginAt("/login?service=" + encodedService);
        assertFormElementPresent(LOGIN_TOKEN);

        // Demonstrate that the second service ticket no longer validates

        beginAt("/serviceValidate?service=" + encodedService + "&ticket=" + secondServiceTicket);
        assertTextPresent("<cas:authenticationFailure");

    }

}
