package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.SoftAssertions.*;
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
    CasCoreTicketsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketCatalogConfiguration.class
})
@DirtiesContext
@Category(RestfulApiCategory.class)
public class RestfulAuthenticationPolicyTests {
    private static final String URI = "http://rest.endpoint.com";

    @BeforeEach
    public void initialize() {
        MockitoAnnotations.initMocks(this);
    }

    private RestfulAuthenticationPolicy newPolicy(final RestTemplate restTemplate) {
        return new RestfulAuthenticationPolicy(restTemplate, URI);
    }

    private MockRestServiceServer newServer(final RestTemplate restTemplate) {
        return MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void verifyPolicyGood() throws Exception {
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
        assertSoftly(softly -> {
            verifyPolicyFails(softly, FailedLoginException.class, HttpStatus.UNAUTHORIZED);
            verifyPolicyFails(softly, AccountLockedException.class, HttpStatus.LOCKED);
            verifyPolicyFails(softly, AccountDisabledException.class, HttpStatus.METHOD_NOT_ALLOWED);
            verifyPolicyFails(softly, AccountDisabledException.class, HttpStatus.FORBIDDEN);
            verifyPolicyFails(softly, AccountNotFoundException.class, HttpStatus.NOT_FOUND);
            verifyPolicyFails(softly, AccountExpiredException.class, HttpStatus.PRECONDITION_FAILED);
            verifyPolicyFails(softly, AccountPasswordMustChangeException.class, HttpStatus.PRECONDITION_REQUIRED);
            verifyPolicyFails(softly, FailedLoginException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }


    private void verifyPolicyFails(final SoftAssertions softly, final Class<? extends Throwable> exceptionClass, final HttpStatus status) {
        val restTemplate = new RestTemplate();
        val mockServer = newServer(restTemplate);
        val policy = newPolicy(restTemplate);

        mockServer.expect(requestTo(URI))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(status));

        val exception = assertThrows(GeneralSecurityException.class, () -> {
            assertTrue(policy.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser"), new LinkedHashSet<>()));
        });
        softly.assertThat(exception).as(status.getReasonPhrase()).hasRootCauseInstanceOf(exceptionClass);
        mockServer.verify();

    }
}
