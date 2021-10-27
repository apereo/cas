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
import net.sourceforge.jwebunit.HttpUnitDialog;

/**
 * Tests the /validate legacy (CAS 1.0) ticket validation service of a CAS server.
 *
 * @author Scott Battaglia
 * @author Andrew Petro
 * @author Drew Mazurek
 * @since 3.0.0
 */
public class ValidateCompatibilityTests extends AbstractCompatibilityTests {

    /**
     * CAS 1.0 response indicating that the ticket was invalid.
     */
    public static final String LEGACY_NO_RESPONSE = "no\n\n";

    public ValidateCompatibilityTests() throws IOException {
        super();
    }

    public ValidateCompatibilityTests(final String name) throws IOException {
        super(name);
    }

    /**
     * Test /validate with no parameters set.
     */
    public void verifyNoParameters() {
        beginAt("/validate");
        assertTextPresent("no");

        // here we test that the response was exactly that specified
        // in section 2.4.2 of the CAS spec
        final HttpUnitDialog htDialog = getDialog();
        final String validateOutput = htDialog.getResponseText();
        final String exactExpectedResponse = LEGACY_NO_RESPONSE;

        assertEquals(exactExpectedResponse, validateOutput);
    }

    /**
     * Test that validating a bad service ticket results in the CAS 1 validation failure
     * response.
     * @throws UnsupportedEncodingException
     */
    public void verifyBadServiceTicket() throws UnsupportedEncodingException {
        final String service = "https://localhost:8443/contacts-cas/j_acegi_cas_security_check";
        beginAt("/validate?service=" + URLEncoder.encode(service, "UTF-8") + "&ticket=test");
        assertTextPresent("no");

        // here we test that the response was exactly that specified
        // in section 2.4.2 of the CAS spec
        final HttpUnitDialog htDialog = getDialog();
        final String validateOutput = htDialog.getResponseText();

        assertEquals(LEGACY_NO_RESPONSE, validateOutput);
    }

    /**
     * Test for the correct CAS1 validation success response.
     * @throws IOException
     */
    public void verifyProperCredentialsAndServiceTicket() throws IOException {

        // log into CAS and obtain a service ticket

        final String service = "http://www.cnn.com";
        beginAt("/login?service=" + URLEncoder.encode(service, "UTF-8"));
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        HttpUnitDialog htDialog = getDialog();

        final String serviceTicket = LoginHelper.serviceTicketFromResponse(htDialog.getResponse());

        beginAt("/validate?service=" + URLEncoder.encode(service, "UTF-8") + "&ticket=" + serviceTicket);
        assertTextPresent("yes");

        // here we test that the response was exactly that specified
        // in section 2.4.2 of the CAS spec
        htDialog = getDialog();
        final String validateOutput = htDialog.getResponseText();

        final String expected = "yes\n" + getUsername() + "\n";

        assertEquals(expected, validateOutput);

        // test that a second validation of the same ticket fails

        beginAt("/validate?service=" + URLEncoder.encode(service, "UTF-8") + "&ticket=" + serviceTicket);

        // here we test that the response was exactly that specified
        // in section 2.4.2 of the CAS spec
        htDialog = getDialog();
        final String secondValidateOutput = htDialog.getResponseText();

        assertEquals(LEGACY_NO_RESPONSE, secondValidateOutput);

    }

    /**
     * Test that validating a ticket for a service other than that declared at
     * validation (declaring different services at /login and at /validate)
     * causes the ticket validation failure response.
     * @throws IOException
     */
    public void verifyServiceMismatch() throws IOException {

        // log into CAS and obtain a service ticket

        final String loginService = "http://www.rutgers.edu";
        final String validateService = "http://www.yale.edu";

        beginAt("/login?service=" + URLEncoder.encode(loginService, "UTF-8"));
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        HttpUnitDialog htDialog = getDialog();

        final String serviceTicket = LoginHelper.serviceTicketFromResponse(htDialog.getResponse());

        beginAt("/validate?service=" + URLEncoder.encode(validateService, "UTF-8") + "&ticket=" + serviceTicket);
        assertTextPresent("no");

        // here we test that the response was exactly that specified
        // in section 2.4.2 of the CAS spec
        htDialog = getDialog();
        final String validateOutput = htDialog.getResponseText();

        assertEquals(LEGACY_NO_RESPONSE, validateOutput);

        // test that validation will now fail even if we specify the right service,
        // that is, that the ticket is now invalid

        beginAt("/validate?service=" + URLEncoder.encode(loginService, "UTF-8") + "&ticket=" + serviceTicket);
        // here we test that the response was exactly that specified
        // in section 2.4.2 of the CAS spec
        htDialog = getDialog();
        final String secondValidateOutput = htDialog.getResponseText();

        assertEquals(LEGACY_NO_RESPONSE, secondValidateOutput);

    }

    /**
     * Test that attempting to validate a ticket without declaring
     * a service returns the ticket validation failure response and
     * invalidates the ticket causing subsequent attempts to validate the
     * ticket to fail with the ticket validation failure response.
     * @throws IOException
     */
    public void verifyNoService() throws IOException {

        // log into CAS and obtain a service ticket
        final String service = "http://www.ja-sig.org";

        beginAt("/login?service=" + URLEncoder.encode(service, "UTF-8"));
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        HttpUnitDialog htDialog = getDialog();

        final String serviceTicket = LoginHelper.serviceTicketFromResponse(htDialog.getResponse());

        beginAt("/validate?ticket=" + serviceTicket);
        assertTextPresent("no");

        // here we test that the response was exactly that specified
        // in section 2.4.2 of the CAS spec
        htDialog = getDialog();
        final String validateOutput = htDialog.getResponseText();

        assertEquals(LEGACY_NO_RESPONSE, validateOutput);

        // whether ticket validation would now succeed if we were to validate
        // specifying the correct service is unspecified, so we do not test it.

    }

    public void verifyNoValidateProxyTickets() {
        //TODO test that validation of a proxy ticket fails.
    }
}
