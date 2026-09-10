package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.validation.AbstractCasProtocolValidationSpecification;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ChainingCasProtocolValidationSpecification;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that the {@code renew} protocol parameter is evaluated per request and never
 * retained on the validation specification beans, which are shared singletons.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@SpringBootTest(classes = {
    BaseCasCoreTests.SharedTestConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasThemesAutoConfiguration.class,
    CasThymeleafAutoConfiguration.class,
    CasValidationAutoConfiguration.class
}, properties = "cas.ticket.st.time-to-kill-in-seconds=120")
@Tag("CAS")
@ExtendWith(CasTestExtension.class)
@Getter
@AutoConfigureMockMvc
class ServiceValidateControllerRenewTests {
    private static final String SUCCESS = "Success";

    private static final String FAILURE = "authenticationFailure";

    private static final Service SERVICE = RegisteredServiceTestUtils.getService("https://www.casinthecloud.com");

    @Autowired
    @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("serviceValidateControllerValidationSpecification")
    private CasProtocolValidationSpecification validationSpecification;

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    /**
     * Collect the specifications that are able to carry {@code renew} state, so a test can assert
     * that a request left none behind. Returning an empty list would make such an assertion
     * vacuous, so callers assert the list is not empty first.
     */
    private static List<AbstractCasProtocolValidationSpecification> renewAwareSpecifications(
        final CasProtocolValidationSpecification specification) {
        if (specification instanceof final ChainingCasProtocolValidationSpecification chain) {
            return chain.getSpecifications()
                .stream()
                .map(ServiceValidateControllerRenewTests::renewAwareSpecifications)
                .flatMap(List::stream)
                .toList();
        }
        return specification instanceof final AbstractCasProtocolValidationSpecification spec
            ? List.of(spec)
            : List.of();
    }

    private void assertNoRetainedRenewState() {
        val specifications = renewAwareSpecifications(validationSpecification);
        assertFalse(specifications.isEmpty(),
            () -> "Unable to observe renew state on [%s]; the assertion below would be vacuous".formatted(validationSpecification));
        assertTrue(specifications.stream().noneMatch(AbstractCasProtocolValidationSpecification::isRenew),
            "A validation request must not leave renew state behind on the shared validation specification");
    }

    @BeforeEach
    void before() {
        ClientInfoHolder.setClientInfo(clientInfo());
    }

    private static ClientInfo clientInfo() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.000");
        request.setLocalAddr("223.456.789.100");
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        return ClientInfo.from(request);
    }

    /**
     * Establish a single sign-on session and consume its first service ticket. A ticket is marked
     * as issued from a new login when credentials were provided <i>or</i> when it is the first
     * ticket handed out by the ticket-granting ticket, so the first one has to be burned before
     * this session can produce tickets that a {@code renew=true} validation must reject.
     *
     * @return the identifier of the ticket-granting ticket backing the session
     */
    private String newSingleSignOnSession() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), SERVICE, ctx);
        return ticketGrantingTicket.getId();
    }

    /**
     * Issue a service ticket off an existing single sign-on session, so the ticket is not issued
     * from a new login and must never satisfy a {@code renew=true} validation.
     * <p>
     * Every ticket gets its own session on purpose. The default session tracking policy only keeps
     * the most recent ticket per service, and it deletes the previous one from the registry, so a
     * single session cannot hand out several usable tickets for the same service.
     */
    private String newSingleSignOnServiceTicket() throws Throwable {
        return getCentralAuthenticationService().grantServiceTicket(newSingleSignOnSession(), SERVICE, null).getId();
    }

    private String validate(final String ticket, final boolean renew) throws Exception {
        val request = get(CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE)
            .param(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId())
            .param(CasProtocolConstants.PARAMETER_TICKET, ticket);
        if (renew) {
            request.param(CasProtocolConstants.PARAMETER_RENEW, "true");
        }
        return mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    @Nested
    class PerRequestEvaluation {
        @Test
        void verifyRenewRejectsTicketFromExistingSingleSignOnSession() throws Throwable {
            val body = validate(newSingleSignOnServiceTicket(), true);
            assertTrue(body.contains(FAILURE));
            assertTrue(body.contains("code=\"%s\"".formatted(CasProtocolConstants.ERROR_CODE_INVALID_TICKET)), body);
        }

        @Test
        void verifyValidationWithoutRenewAcceptsTicketFromExistingSingleSignOnSession() throws Throwable {
            assertTrue(validate(newSingleSignOnServiceTicket(), false).contains(SUCCESS));
        }

        @Test
        void verifyRenewIsNotRetainedOnTheSharedSpecification() throws Throwable {
            assertNoRetainedRenewState();
            assertTrue(validate(newSingleSignOnServiceTicket(), true).contains(FAILURE));
            assertNoRetainedRenewState();
        }
    }

    @Nested
    class ConcurrentEvaluation {
        @Test
        void verifyConcurrentRenewAndNonRenewValidationsDoNotInterfere() throws Throwable {
            val iterations = 25;
            val renewTickets = new ArrayList<String>(iterations);
            val ssoTickets = new ArrayList<String>(iterations);
            for (var i = 0; i < iterations; i++) {
                renewTickets.add(newSingleSignOnServiceTicket());
                ssoTickets.add(newSingleSignOnServiceTicket());
            }

            val clientInfo = clientInfo();
            val failures = new CopyOnWriteArrayList<String>();
            val tasks = new ArrayList<Callable<Void>>(iterations * 2);
            for (var i = 0; i < iterations; i++) {
                val renewTicket = renewTickets.get(i);
                val ssoTicket = ssoTickets.get(i);
                tasks.add(() -> {
                    ClientInfoHolder.setClientInfo(clientInfo);
                    if (!validate(renewTicket, true).contains(FAILURE)) {
                        failures.add("renew=true validated ticket %s that was not issued from a new login".formatted(renewTicket));
                    }
                    return null;
                });
                tasks.add(() -> {
                    ClientInfoHolder.setClientInfo(clientInfo);
                    if (!validate(ssoTicket, false).contains(SUCCESS)) {
                        failures.add("validation without renew rejected ticket %s".formatted(ssoTicket));
                    }
                    return null;
                });
            }

            try (val executor = Executors.newFixedThreadPool(4)) {
                for (val future : executor.invokeAll(tasks)) {
                    future.get();
                }
            }
            assertTrue(failures.isEmpty(), failures::toString);
            assertNoRetainedRenewState();
        }
    }
}
