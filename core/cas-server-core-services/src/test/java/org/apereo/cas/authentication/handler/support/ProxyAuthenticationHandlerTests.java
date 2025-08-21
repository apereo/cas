package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
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
class ProxyAuthenticationHandlerTests {

    private AuthenticationHandler authenticationHandler;

    @BeforeEach
    void initialize() {
        authenticationHandler = new ProxyAuthenticationHandler(StringUtils.EMPTY, null, 0, new SimpleHttpClientFactoryBean().getObject());
    }

    @Test
    void verifySupportsProperUserCredentials() {
        assertTrue(authenticationHandler.supports(RegisteredServiceTestUtils.getHttpBasedServiceCredentials()));
        assertTrue(authenticationHandler.supports(RegisteredServiceTestUtils.getHttpBasedServiceCredentials().getClass()));
    }

    @Test
    void verifyDoesntSupportBadUserCredentials() {
        assertFalse(authenticationHandler.supports(
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test2")));
    }

    @Test
    void verifyAcceptsProperCertificateCredentials() throws Throwable {
        val credentials = RegisteredServiceTestUtils.getHttpBasedServiceCredentials();
        credentials.setCredentialMetadata(new BasicCredentialMetadata(new BasicIdentifiableCredential("helloworld")));
        val result = authenticationHandler.authenticate(credentials, RegisteredServiceTestUtils.getService());
        assertNotNull(result);
        assertEquals("helloworld", result.getPrincipal().getId());
    }

    @Test
    void verifyRejectsInProperCertificateCredentials() {
        assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials(
            "https://clearinghouse.ja-sig.org"), RegisteredServiceTestUtils.getService()));
    }

    @Test
    void verifyAcceptsNonHttpsCredentials() throws Throwable {
        assertNotNull(authenticationHandler.authenticate(
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials("http://http.badssl.com/"), RegisteredServiceTestUtils.getService()));
    }

    @Test
    void verifyNoAcceptableStatusCode() {
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(
                RegisteredServiceTestUtils.getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu"),
                RegisteredServiceTestUtils.getService()));
    }

    @Test
    void verifyNoAcceptableStatusCodeButOneSet() {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setAcceptableCodes(CollectionUtils.wrapList(900));
        val httpClient = clientFactory.getObject();
        authenticationHandler = new ProxyAuthenticationHandler(StringUtils.EMPTY, null, null, httpClient);
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(
                RegisteredServiceTestUtils.getHttpBasedServiceCredentials("https://www.ja-sig.org"),
                RegisteredServiceTestUtils.getService()));
    }
}
