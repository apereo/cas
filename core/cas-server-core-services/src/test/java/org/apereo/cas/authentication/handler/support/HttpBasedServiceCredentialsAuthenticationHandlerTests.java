package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;

import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class HttpBasedServiceCredentialsAuthenticationHandlerTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler;

    @BeforeEach
    public void initialize() {
        this.authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler("", null, null, null, new SimpleHttpClientFactoryBean().getObject());
    }

    @Test
    public void verifySupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(RegisteredServiceTestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test2")));
    }

    @Test
    public void verifyAcceptsProperCertificateCredentials() throws Exception {
        assertNotNull(this.authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void verifyRejectsInProperCertificateCredentials() throws Exception {
        this.thrown.expect(FailedLoginException.class);


        this.authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials("https://clearinghouse.ja-sig.org"));
    }

    @Test
    public void verifyAcceptsNonHttpsCredentials() throws Exception {
        assertNotNull(this.authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials("http://www.google.com")));
    }

    @Test
    public void verifyNoAcceptableStatusCode() throws Exception {
        this.thrown.expect(FailedLoginException.class);
        this.authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu"));
    }

    @Test
    public void verifyNoAcceptableStatusCodeButOneSet() throws Exception {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setAcceptableCodes(CollectionUtils.wrapList(900));
        val httpClient = clientFactory.getObject();
        this.authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler("", null, null, null, httpClient);
        this.thrown.expect(FailedLoginException.class);
        this.authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials("https://www.ja-sig.org"));
    }
}
