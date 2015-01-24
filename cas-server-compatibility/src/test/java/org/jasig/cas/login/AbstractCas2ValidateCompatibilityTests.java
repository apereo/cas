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
 * Common test cases for /serviceValidate and /proxyValidate.
 * @since 3.0.0
 */
public abstract class AbstractCas2ValidateCompatibilityTests extends AbstractCompatibilityTests {

    /**
     * The name of the compatibility test configuration property the value of which
     * will be the URL of a proxy ticket receptor to which the CAS server under test
     * can send proxy granting tickets.  E.g., an instance of the Java CAS Client
     * ProxyTicketReceptor servlet deployed behind SSL with an SSL cert trusted by the
     * CAS server under test.  We use this to test specifying a proxy callback URL
     * and thereby obtaining a pgtiou.
     */
    public static final String PROXY_RECEPTOR_URL_PROPERTY = "pgtreceptor.url";

    public AbstractCas2ValidateCompatibilityTests() throws IOException {
        super();
    }

    public AbstractCas2ValidateCompatibilityTests(final String name) throws IOException {
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

    protected final String getProxyCallbackUrl() {
        return getProperties().getProperty(PROXY_RECEPTOR_URL_PROPERTY);
    }

    public void verifyNoParameters() {
        beginAt(getValidationPath());
        assertTextPresent("cas:authenticationFailure");

        // TODO actually test the validation response XML.
    }

    public void verifyBadServiceTicket() throws UnsupportedEncodingException {
        final String service = getServiceUrl();
        beginAt(getValidationPath() + "?service=" + URLEncoder.encode(service, "UTF-8") + "&ticket=test");

        assertTextPresent("cas:authenticationFailure");

        // TODO do more to test that the response is actually XML, etc. etc.
    }

    /**
     * Test validation of a valid service ticket and that service tickets are
     * not multiply validatable.
     * @throws IOException
     */
    public void verifyProperCredentialsAndServiceTicket() throws IOException {
        final String service = getServiceUrl();
        final String encodedService = URLEncoder.encode(service, "UTF-8");
        beginAt("/login?service=" + encodedService);
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        // read the service ticket

        final String serviceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // great, now we have a ticket

        // let's validate it

        beginAt(getValidationPath() + "?service=" + encodedService + "&" + "ticket=" + serviceTicket);

        assertTextPresent("cas:authenticationSuccess");

        // this assertion may be too strict.  How does whitespace work here?
        assertTextPresent("<cas:user>" + getUsername() + "</cas:user>");

        // TODO do more to test that the response is actually XML, etc. etc.

        // let's validate it again and ensure that we cannot again validate
        // the ticket

        beginAt(getValidationPath() + "?service=" + encodedService + "&" + "ticket=" + serviceTicket);
        assertTextPresent("cas:authenticationFailure");

        // TODO do more to test that the response is actually XML, etc. etc.

    }

    /**
     * Test that renew=true, when specified both at login and ticket validation,
     * validation succeeds.
     * @throws IOException
     */
    public void verifyRenew() throws IOException {
        final String service = getServiceUrl();
        final String encodedService = URLEncoder.encode(service, "UTF-8");
        beginAt("/login?renew=true&service=" + encodedService);
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        // read the service ticket

        final String serviceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // great, now we have a ticket

        // let's validate it

        beginAt(getValidationPath() + "?renew=true&service=" + encodedService + "&" + "ticket=" + serviceTicket);

        assertTextPresent("cas:authenticationSuccess");
    }

    /**
     * Test a couple renew=true logins...
     * @throws IOException
     */
    public void verifyMultipleRenew() throws IOException {
        final String service = getServiceUrl();
        final String encodedService = URLEncoder.encode(service, "UTF-8");

        beginAt("/login?renew=true&service=" + encodedService);
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        // read the service ticket

        String serviceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // great, now we have a ticket

        // let's validate it

        beginAt(getValidationPath() + "?service=" + encodedService + "&" + "ticket=" + serviceTicket + "&renew=true");

        assertTextPresent("cas:authenticationSuccess");

        // now let's do it again

        beginAt("/login?renew=true&service=" + encodedService);
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        // read the service ticket

        serviceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // great, now we have a ticket

        // let's validate it

        beginAt(getValidationPath() + "?service=" + encodedService + "&" + "ticket=" + serviceTicket + "&renew=true");

        assertTextPresent("<cas:authenticationSuccess>");

    }

    /**
     * Test that renew=true, when specified only at ticket validation,
     * validation succeeds if username, password were presented at login even
     * though renew wasn't set then.
     * @throws IOException
     */
    public void verifyAccidentalRenew() throws IOException {
        final String service = getServiceUrl();
        final String encodedService = URLEncoder.encode(service, "UTF-8");
        beginAt("/login?service=" + encodedService);
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        // read the service ticket

        final String serviceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // great, now we have a ticket

        // let's validate it

        beginAt(getValidationPath() + "?renew=true&service=" + encodedService + "&" + "ticket=" + serviceTicket);

        assertTextPresent("<cas:authenticationSuccess>");
        assertTextPresent("<cas:user>" + getUsername() + "</cas:user>");
    }

    /**
     * Test that renew at ticket validation blocks validation of a ticket
     * vended via SSO.
     * @throws IOException
     */
    public void verifyRenewBlocksSsoValidation() throws IOException {

        // initial authentication
        final String firstService = getServiceUrl();
        final String encodedFirstService = URLEncoder.encode(firstService, "UTF-8");
        beginAt("/login?service=" + encodedFirstService);
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        final String firstServiceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // that established SSO.  Now let's get another ticket via SSO

        final String secondService= "http://www.uportal.org/";
        final String encodedSecondService = URLEncoder.encode(secondService, "UTF-8");

        beginAt("/login?service=" + encodedSecondService);

        // read the service ticket

        final String secondServiceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // let's validate the second (non-renew) ticket.

        beginAt(getValidationPath() + "?renew=true&service=" + encodedSecondService + "&ticket=" + secondServiceTicket);

        assertTextPresent("cas:authenticationFailure");

        //TODO test the authentication failure response in more detail

        assertTextNotPresent("<cas:user>");

        // however, we can validate the first ticket with renew=true.

        beginAt(getValidationPath() + "?renew=true&service=" + encodedFirstService + "&ticket=" + firstServiceTicket);

        assertTextPresent("cas:authenticationSuccess");
        assertTextPresent("<cas:user>" + getUsername() + "</cas:user>");
        //TODO assert more about the response

    }

    /**
     * Test best-effort ticket validation when a specified proxy callback handler
     * doesn't really exist.
     * @throws IOException
     */
    public void verifyBrokenProxyCallbackUrl() throws IOException {

        final String service = getServiceUrl();
        final String encodedService = URLEncoder.encode(service, "UTF-8");
        beginAt("/login?service=" + encodedService);
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        // read the service ticket

        final String serviceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // great, now we have a ticket

        // let's validate it, specifying a bogus pgt callback

        final String encodedProxyCallbackUrl = URLEncoder.encode("https://secure.its.yale.edu/cas/noexist", "UTF-8");

        beginAt(getValidationPath() + "?renew=true&service=" + encodedService + "&" + "ticket="
                                    + serviceTicket + "&pgtUrl=" + encodedProxyCallbackUrl);

        assertTextPresent("<cas:authenticationSuccess>");
        assertTextPresent("<cas:user>" + getUsername() + "</cas:user>");

        // no pgtiou because failure in sending pgt to specified receptor URL.
        assertTextNotPresent("<cas:pgtiou>");

    }

    public void verifyPgtAcquisition() throws IOException {

        final String service = getServiceUrl();
        final String encodedService = URLEncoder.encode(service, "UTF-8");
        beginAt("/login?service=" + encodedService);
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        // read the service ticket

        final String serviceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // great, now we have a ticket

        // let's validate it, specifying a bogus pgt callback

        final String encodedProxyCallbackUrl = URLEncoder.encode(getProxyCallbackUrl(), "UTF-8");

        beginAt(getValidationPath() + "?renew=true&service=" + encodedService + "&" + "ticket=" + serviceTicket
                                    + "&pgtUrl=" + encodedProxyCallbackUrl);

        assertTextPresent("<cas:authenticationSuccess>");
        assertTextPresent("<cas:user>" + getUsername() + "</cas:user>");
        // pgtiou because success in sending pgt
        assertTextPresent("<cas:proxyGrantingTicket>");

    }

    /**
     * Test for JIRA issue CAS-224.
     * 1. /cas/login?service=foo&renew=true
     * 2. log in
     * 3. /serviceValidate?ticket=[ticket]&service=foo&renew=true
     *
     * Issue was that validation fails whereas it should succeed.
     *
     * This testcase is almost certainly redundant, but it's explicitly here
     * to cover this issue.
     * @throws IOException
     */
    public void verify224() throws IOException {

        final String service = getServiceUrl();
        final String encodedService = URLEncoder.encode(service, "UTF-8");

        beginAt("/login?service=" + encodedService + "&renew=true");
        setFormElement("username", getUsername());
        setFormElement("password", getGoodPassword());
        submit();

        // read the service ticket

        final String serviceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());

        // great, now we have a ticket

        // let's validate it
        assertNotNull(serviceTicket);

        beginAt(getValidationPath() + "?ticket=" + serviceTicket + "&service=" + encodedService + "&renew=true");

        assertTextPresent("cas:authenticationSuccess");

    }

}
