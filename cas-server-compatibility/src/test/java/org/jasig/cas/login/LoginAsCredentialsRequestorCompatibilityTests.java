/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
 *
 * @author Scott Battaglia
 * @author Drew Mazurek
 * @since 3.0
 *
 */
public class LoginAsCredentialsRequestorCompatibilityTests extends AbstractLoginCompatibilityTests {

    public LoginAsCredentialsRequestorCompatibilityTests() throws IOException {
        super();
    }

    public LoginAsCredentialsRequestorCompatibilityTests(final String name) throws IOException {
        super(name);
    }

    public void testLoginWithNoParams() {
        final String URL = "/login";
        beginAt(URL);
        assertFormElementPresent(LOGIN_TOKEN);
    }

    public void testGatewayWithServiceWithNoTgt() throws UnsupportedEncodingException {
        final String GATEWAY = "true";
        final String SERVICE = URLEncoder.encode("http://www.cnn.com", "UTF-8");
        final String URL = "/login?service=" + SERVICE + "&gateway=" + GATEWAY;

        beginAt(URL);

        // test that we're now at cnn.com rather than at the login form.
        assertTextPresent("cnn.com");
        assertFormElementNotPresent("lt");
    }

    public void testBlankGateway() throws UnsupportedEncodingException {
        final String SERVICE = URLEncoder.encode("http://www.cnn.com", "UTF-8");
        final String URL = "/login?service=" + SERVICE + "&gateway=";

        beginAt(URL);

        // test that we're now at cnn.com rather than at the login form.
        assertTextPresent("cnn.com");
        assertFormElementNotPresent("lt");
    }

    /**
     * Test that setting gateway explicitly to "false" behaves as if gateway
     * were set to true, since the spec for gateway is present / not-present.
     * @throws UnsupportedEncodingException
     */
    public void testGatewayFalseEqualsGatewayTrueWithServiceWithNoTgt() throws UnsupportedEncodingException {
        final String GATEWAY = "false";
        final String SERVICE = URLEncoder.encode("http://www.cnn.com", "UTF-8");
        final String URL = "/login?service=" + SERVICE + "&gateway=" + GATEWAY;

        beginAt(URL);
        assertTextPresent("cnn.com");
    }

    public void testServiceWithSingleSignOn() {
        beginAt("/login");
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        final String URL = "/login";
        submit();
        assertCookiePresent(COOKIE_TGC_ID);
        beginAt(URL);
        assertFormNotPresent(FORM_USERNAME);
    }

    /**
     * Test for recommended behavior in case where no service is specified and
     * gateway is set.  Recommended behavior is that CAS behave as if neither
     * service nor gateway had been set (provide opportunity to establish
     * SSO session).
     *
     * CAS server instances failing this test may not be non-compliant -
     * not following the recommended behavior can cause this test case to fail.
     */
    public void testGatewayWithNoService() {
        final String GATEWAY = "notNull";
        final String URL = "/login?gateway=" + GATEWAY;

        beginAt(URL);
        assertFormElementPresent(LOGIN_TOKEN);
    }

    /**
     * Test that visiting login with gateway=true yields a valid service ticket
     * without painting the login screen.
     * @throws IOException
     */
    public void testGatewayWithServiceWithTgt() throws IOException {
        final String GATEWAY = "notNull";
        final String service = "http://www.yale.edu";
        final String encodedService = URLEncoder.encode(service, "UTF-8");
        final String URLNOGW = "/login?service=" + encodedService;
        final String URLGW = "/login?service=" + encodedService + "&gateway=" + GATEWAY;

        beginAt(URLNOGW);

        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();

        beginAt(URLGW);

        // extract the service ticket
        String st = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // be sure it's valid
        assertNotNull(st);

        beginAt("/validate?ticket=" + st + "&service=" + encodedService);
        HttpUnitDialog htDialog = getDialog();
        String validateOutput = htDialog.getResponseText();

        String expected = "yes\n" + getUsername() + "\n";

        assertEquals(expected, validateOutput);

    }

    /**
     * Test that /login?gateway=&service=whatever is the same as /login?gateway=true&service=whatever.
     * @throws IOException
     */
    public void testGatewayEqualsBlankWithServiceWithTgt() throws IOException {
        final String service = "http://www.yale.edu";
        final String encodedService = URLEncoder.encode(service, "UTF-8");
        final String establishSsoUrl = "/login?service=" + encodedService;

        beginAt(establishSsoUrl);

        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();

        final String gatewayUrl = "/login?service=" + encodedService + "&gateway=";
        beginAt(gatewayUrl);

        // extract the service ticket
        String st = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // be sure it's valid
        assertNotNull(st);


        beginAt("/validate?ticket=" + st + "&service=" + encodedService);
        HttpUnitDialog htDialog = getDialog();
        String validateOutput = htDialog.getResponseText();

        String expected = "yes\n" + getUsername() + "\n";

        assertEquals(expected, validateOutput);

    }

    /**
     * Test that when renew=true, an existing TGT still causes CAS to render
     * the login UI.
     * @throws UnsupportedEncodingException
     */
    public void testExistingTgtRenewEqualsTrue() throws UnsupportedEncodingException {
        final String service = "http://www.yale.edu";
        final String encodedService = URLEncoder.encode(service, "UTF-8");
        final String renewUrl = "/login?service=" + encodedService + "&renew=true";

        beginAt("/login");
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();
        assertCookiePresent(COOKIE_TGC_ID);
        beginAt(renewUrl);

        // test that we're at the login screen (no ST was issued).
        assertFormPresent();
        assertFormElementPresent(LOGIN_TOKEN);
    }

    /**
     * Test that when the request parameter 'renew' is set at all,
     * an existing TGT still causes CAS to render the login UI.
     * @throws UnsupportedEncodingException
     */
    public void testExistingTgtRenewEqualsNonNull() throws UnsupportedEncodingException {
        final String service = getServiceUrl();
        final String encodedService = URLEncoder.encode(service, "UTF-8");
        final String nonNullRenewUrl = "/login?service=" + encodedService + "&renew=nonnull";

        beginAt("/login");
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();
        assertCookiePresent(COOKIE_TGC_ID);
        beginAt(nonNullRenewUrl);

        // test that we're at the login screen (no ST was issued).
        assertFormPresent();
        assertFormElementPresent(LOGIN_TOKEN);

        // test what when renew "is set" but no particular value is given
        // CAS server behaves as if renew=true
        final String renewSetUrl = "/login?service=" + encodedService + "&renew";
        beginAt(renewSetUrl);
        // test that we're at the login screen (no ST was issued).
        assertFormPresent();
        assertFormElementPresent(LOGIN_TOKEN);


    }

    public void testTrustHandling() {
        // TODO test trust handling
    }

    public void testInitialFormParameters() {
        beginAt("/login");
        assertFormElementPresent(FORM_USERNAME);
        assertFormElementPresent(FORM_PASSWORD);
        assertFormElementPresent(LOGIN_TOKEN);
    }

    /**
     * Test that the renew parameter on /login overrides the gateway parameter,
     * as recommended in the CAS 2 spec S 2.1.1.
     *
     * A CAS server instance failing this test may not be incompatible, only
     * failing to follow a recommendation.
     * @throws UnsupportedEncodingException
     */
    public void testRenewOverridesGateway() throws UnsupportedEncodingException {
        // first, establish SSO
        final String service = "http://www.yale.edu";
        final String encodedService = URLEncoder.encode(service, "UTF-8");

        beginAt("/login");


        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();
        assertCookiePresent(COOKIE_TGC_ID);

        // then, hit login with a service, renew, and gateway
        final String renewAndGatewayUrl = "/login?service=" + encodedService + "&renew=true&gateway=true";
        beginAt(renewAndGatewayUrl);

        // test that we're at the login screen (no ST was issued).
        assertFormPresent();
        assertFormElementPresent(LOGIN_TOKEN);

    }
}
