package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.LinkedHashSet;

import static org.apereo.cas.util.junit.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * This is {@link RestfulAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketCatalogConfiguration.class
})
@DirtiesContext
@Tag("RestfulApi")
public class RestfulAuthenticationPolicyTests {
    private static final String URI = "http://rest.endpoint.com";

    @BeforeEach
    public void initialize() {
        MockitoAnnotations.initMocks(this);
    }

    private static RestfulAuthenticationPolicy newPolicy(final RestTemplate restTemplate) {
        return new RestfulAuthenticationPolicy(restTemplate, URI);
    }

    private static MockRestServiceServer newServer(final RestTemplate restTemplate) {
        return MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @SneakyThrows
    public void verifyPolicyGood() {
        val restTemplate = new RestTemplate();
        val mockServer = newServer(restTemplate);
        val policy = newPolicy(restTemplate);

        mockServer.expect(requestTo(URI))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess());
        assertTrue(policy.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser"), new LinkedHashSet<>()));
        mockServer.verify();
    }

    @Test
    public void verifyPolicyFailsWithStatusCodes() {
        assertAll(() -> {
            assertPolicyFails(FailedLoginException.class, HttpStatus.UNAUTHORIZED);
            assertPolicyFails(AccountLockedException.class, HttpStatus.LOCKED);
            assertPolicyFails(AccountDisabledException.class, HttpStatus.METHOD_NOT_ALLOWED);
            assertPolicyFails(AccountDisabledException.class, HttpStatus.FORBIDDEN);
            assertPolicyFails(AccountNotFoundException.class, HttpStatus.NOT_FOUND);
            assertPolicyFails(AccountExpiredException.class, HttpStatus.PRECONDITION_FAILED);
            assertPolicyFails(AccountPasswordMustChangeException.class, HttpStatus.PRECONDITION_REQUIRED);
            assertPolicyFails(FailedLoginException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }


    private static void assertPolicyFails(final Class<? extends Throwable> exceptionClass, final HttpStatus status) {
        val restTemplate = new RestTemplate();
        val mockServer = newServer(restTemplate);
        val policy = newPolicy(restTemplate);

        mockServer.expect(requestTo(URI))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(status));

        assertThrowsWithRootCause(GeneralSecurityException.class, exceptionClass,
            () -> assertTrue(policy.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser"), new LinkedHashSet<>())));
        mockServer.verify();
    }
}
