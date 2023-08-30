package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("AuthenticationHandler")
class HttpBasedServiceCredentialsAuthenticationHandlerTests {

    private HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler;

    @BeforeEach
    public void initialize() {
        authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler(StringUtils.EMPTY, null, null, null, new SimpleHttpClientFactoryBean().getObject());
    }

    @Test
    void verifySupportsProperUserCredentials() throws Throwable {
        assertTrue(authenticationHandler.supports(RegisteredServiceTestUtils.getHttpBasedServiceCredentials()));
        assertTrue(authenticationHandler.supports(RegisteredServiceTestUtils.getHttpBasedServiceCredentials().getClass()));
    }

    @Test
    void verifyDoesntSupportBadUserCredentials() throws Throwable {
        assertFalse(authenticationHandler.supports(
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test2")));
    }

    @Test
    void verifyAcceptsProperCertificateCredentials() throws Throwable {
        assertNotNull(authenticationHandler.authenticate(
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials(), RegisteredServiceTestUtils.getService()));
    }

    @Test
    void verifyRejectsInProperCertificateCredentials() throws Throwable {
        assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials(
            "https://clearinghouse.ja-sig.org"), RegisteredServiceTestUtils.getService()));
    }

    @Test
    void verifyAcceptsNonHttpsCredentials() throws Throwable {
        assertNotNull(authenticationHandler.authenticate(
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials("http://www.google.com"), RegisteredServiceTestUtils.getService()));
    }

    @Test
    void verifyNoAcceptableStatusCode() throws Throwable {
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(
                RegisteredServiceTestUtils.getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu"),
                RegisteredServiceTestUtils.getService()));
    }

    @Test
    void verifyNoAcceptableStatusCodeButOneSet() throws Throwable {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setAcceptableCodes(CollectionUtils.wrapList(900));
        val httpClient = clientFactory.getObject();
        authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler(StringUtils.EMPTY, null, null, null, httpClient);
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(
                RegisteredServiceTestUtils.getHttpBasedServiceCredentials("https://www.ja-sig.org"),
                RegisteredServiceTestUtils.getService()));
    }
}
