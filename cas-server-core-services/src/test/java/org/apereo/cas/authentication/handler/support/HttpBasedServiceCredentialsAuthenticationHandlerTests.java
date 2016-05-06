package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.services.TestUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.login.FailedLoginException;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class HttpBasedServiceCredentialsAuthenticationHandlerTests {

    private HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler();
        this.authenticationHandler.setHttpClient(new SimpleHttpClientFactoryBean().getObject());
    }

    @Test
    public void verifySupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(TestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test2")));
    }

    @Test
    public void verifyAcceptsProperCertificateCredentials() throws Exception {
        assertNotNull(this.authenticationHandler.authenticate(TestUtils.getHttpBasedServiceCredentials()));
    }

    @Test(expected = FailedLoginException.class)
    public void verifyRejectsInProperCertificateCredentials() throws Exception {
        this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials("https://clearinghouse.ja-sig.org"));
    }

    @Test
    public void verifyAcceptsNonHttpsCredentials() throws Exception {
        this.authenticationHandler.setHttpClient(new SimpleHttpClientFactoryBean().getObject());
        assertNotNull(this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials("http://www.google.com")));
    }

    @Test(expected = FailedLoginException.class)
    public void verifyNoAcceptableStatusCode() throws Exception {
        this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu"));
    }

    @Test(expected = FailedLoginException.class)
    public void verifyNoAcceptableStatusCodeButOneSet() throws Exception {
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setAcceptableCodes(new int[] {900});
        final HttpClient httpClient = clientFactory.getObject();
        this.authenticationHandler.setHttpClient(httpClient);
        this.authenticationHandler.authenticate(TestUtils.getHttpBasedServiceCredentials("https://www.ja-sig.org"));
    }
}
