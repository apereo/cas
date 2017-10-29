package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.hamcrest.CustomMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.Assert.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * This is {@link RestfulAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
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
public class RestfulAuthenticationPolicyTests {
    private static final String URI = "http://rest.endpoint.com";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private RestTemplate restTemplate = new RestTemplate();

    private MockRestServiceServer mockServer;

    private RestfulAuthenticationPolicy policy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockServer = MockRestServiceServer.createServer(restTemplate);
        policy = new RestfulAuthenticationPolicy(this.restTemplate, URI);
    }

    @Test
    public void verifyPolicyGood() throws Exception {
        mockServer.expect(requestTo(URI))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());
        assertTrue(policy.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser")));
        mockServer.verify();
    }

    @Test
    public void verifyPolicyFailsWithStatusCodes() throws Exception {
        verifyPolicyFails(FailedLoginException.class, HttpStatus.UNAUTHORIZED);
        verifyPolicyFails(AccountLockedException.class, HttpStatus.LOCKED);
        verifyPolicyFails(AccountDisabledException.class, HttpStatus.METHOD_NOT_ALLOWED);
        verifyPolicyFails(AccountDisabledException.class, HttpStatus.FORBIDDEN);
        verifyPolicyFails(AccountNotFoundException.class, HttpStatus.NOT_FOUND);
        verifyPolicyFails(AccountExpiredException.class, HttpStatus.PRECONDITION_FAILED);
        verifyPolicyFails(AccountPasswordMustChangeException.class, HttpStatus.PRECONDITION_REQUIRED);
        verifyPolicyFails(FailedLoginException.class, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private void verifyPolicyFails(final Class exceptionClass, final HttpStatus status) throws Exception {
        thrown.expectCause(new CustomMatcher<Throwable>("policy") {
            @Override
            public boolean matches(final Object o) {
                return o.getClass().equals(exceptionClass);
            }
        });

        mockServer.expect(requestTo(URI))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(status));
        assertTrue(policy.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser")));
        mockServer.verify();
    }
}
