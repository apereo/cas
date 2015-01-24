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
 * @author Drew Mazurek
 * @since 3.0.0
 *
 */
public class LoginAsCredentialsAcceptorCompatibilityTests extends AbstractLoginCompatibilityTests {

    public LoginAsCredentialsAcceptorCompatibilityTests() throws IOException {
        super();
    }

    public LoginAsCredentialsAcceptorCompatibilityTests(final String name) throws IOException {
        super(name);
    }

    public void verifySingleSignOn() {
        beginAt("/login");
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();
        assertCookiePresent(COOKIE_TGC_ID);
        assertFormNotPresent();

        // TODO test logging in to another service
    }

    public void verifyValidCredentialsAuthenticationWithWarn() throws IOException {
        final String service = "http://www.yale.edu";
        beginAt("/login?service=" + URLEncoder.encode(service, "UTF-8"));
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        getDialog().getForm().setCheckbox("warn", true);
        submit();

        final String anotherService = "https://secure.its.yale.edu/cas";
        final String anotherServiceEncoded = URLEncoder.encode(anotherService, "UTF-8");

        beginAt("/login?service=" + anotherServiceEncoded);

        // since warn was set, CAS should not redirect us immediately to the service,
        // but should rather interpose a warning screen.

        assertTextPresent(anotherService);


    }

    public void verifyValidCredentialsAuthenticationWithoutWarn() throws UnsupportedEncodingException {
        final String service = "http://www.cnn.com";
        beginAt("/login?service=" + URLEncoder.encode(service, "UTF-8"));
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();
        // TODO testValidCredentialsAuthenticationWithoutWarn
    }

    /*
     * jWebUnit doesn't allow you to change pre-populated hidden form values.
     *
    public void verifyBadLoginTicket() {
        setFormElement(FORM_USERNAME, "test");
        setFormElement(FORM_PASSWORD, "test");
        setFormElement(WebConstants.LOGIN_TOKEN, "test");

        submit();
        assertFormElementPresent(FORM_USERNAME);
    }

    public void verifyNoLoginTicket() {
        setFormElement(FORM_USERNAME, "test");
        setFormElement(FORM_PASSWORD, "test");
        setFormElement(WebConstants.LOGIN_TOKEN, "");
        submit();
        assertFormElementPresent(FORM_USERNAME);
    }

    public void verifyDoubleLoginTicket() {
        //TODO covered by badLoginTicket?
    }
     *
     */

    public void verifyPassBadCredentials() {
        beginAt("/login");
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getBadPassword());
        submit();
        assertFormElementPresent(FORM_USERNAME);
    }

    public void verifyPassEmptyCredentials() {
        beginAt("/login");
        submit();
        assertFormElementPresent(FORM_USERNAME);
    }

    /**
     * Test that logging in as someone else destroys the TGT and outstanding
     * service tickets for the previously authenticated user.
     * @throws IOException
     */
    public void verifyLoginAsSomeoneElse() throws IOException {
        final String encodedService = URLEncoder.encode(getServiceUrl(), "UTF-8");

        // establish SSO session as the first user

        beginAt("/login?service=" + encodedService);
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();

        // get the service ticket

        final String firstServiceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // now login via renew as someone else


        beginAt("/login?renew=true&service=" + encodedService);
        setFormElement(FORM_USERNAME, getAlternateUsername());
        setFormElement(FORM_PASSWORD, getAlternatePassword());
        submit();

        // get the service ticket

        final String secondServiceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // validate the second service ticket

        beginAt("/serviceValidate?ticket=" + secondServiceTicket + "&service=" + encodedService);

        assertTextPresent("<cas:user>" + getAlternateUsername() + "</cas:user>");


        // okay, now attempt to validate the original service ticket
        // and see that it has been invalidated

        beginAt("/serviceValidate?ticket=" + firstServiceTicket + "&service=" + encodedService);
        assertTextPresent("<cas:authenticationFailure");

    }

}
