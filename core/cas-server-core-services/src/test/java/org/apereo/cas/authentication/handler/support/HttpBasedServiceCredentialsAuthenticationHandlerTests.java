package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;

import lombok.SneakyThrows;
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
@Tag("Simple")
public class HttpBasedServiceCredentialsAuthenticationHandlerTests {

    private HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler;

    @BeforeEach
    public void initialize() {
        this.authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler(StringUtils.EMPTY, null, null, null, new SimpleHttpClientFactoryBean().getObject());
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
    @SneakyThrows
    public void verifyAcceptsProperCertificateCredentials() {
        assertNotNull(this.authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void verifyRejectsInProperCertificateCredentials() {
        assertThrows(FailedLoginException.class, () -> this.authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials(
            "https://clearinghouse.ja-sig.org")));
    }

    @Test
    @SneakyThrows
    public void verifyAcceptsNonHttpsCredentials() {
        assertNotNull(this.authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials("http://www.google.com")));
    }

    @Test
    public void verifyNoAcceptableStatusCode() {
        assertThrows(FailedLoginException.class,
            () -> this.authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu")));
    }

    @Test
    public void verifyNoAcceptableStatusCodeButOneSet() {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setAcceptableCodes(CollectionUtils.wrapList(900));
        val httpClient = clientFactory.getObject();
        this.authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler(StringUtils.EMPTY, null, null, null, httpClient);
        assertThrows(FailedLoginException.class,
            () -> this.authenticationHandler.authenticate(RegisteredServiceTestUtils.getHttpBasedServiceCredentials("https://www.ja-sig.org")));
    }
}
