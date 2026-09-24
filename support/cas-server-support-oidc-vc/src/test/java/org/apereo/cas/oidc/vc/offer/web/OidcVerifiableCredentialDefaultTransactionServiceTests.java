package org.apereo.cas.oidc.vc.offer.web;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcVerifiableCredentialDefaultTransactionServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDC")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
class OidcVerifiableCredentialDefaultTransactionServiceTests extends AbstractOidcTests {
    @Autowired
    @Qualifier(OidcVerifiableCredentialTransactionService.BEAN_NAME)
    private OidcVerifiableCredentialTransactionService oidcVerifiableCredentialTransactionService;

    @Test
    void verifyIssueOperation() {
        val registeredService = getOidcRegisteredService();
        val credentialConfigIds = List.of("UniversityDegreeCredential", "DriverLicenseCredential");
        val ticket = oidcVerifiableCredentialTransactionService.issue(registeredService.getClientId(), "casuser", credentialConfigIds);
        assertNotNull(ticket);
        assertNotNull(ticket.getId());
        assertTrue(ticket.getId().startsWith(TransientSessionTicket.PREFIX));

        val sessionTicket = (TransientSessionTicket) ticketRegistry.getTicket(ticket.getId());
        assertNotNull(sessionTicket);
        assertEquals("casuser", sessionTicket.getProperty("principalId", String.class));
        assertEquals(credentialConfigIds, sessionTicket.getProperty("credentialConfigurationIds", List.class));
        assertNotNull(sessionTicket.getProperty("issuerState", String.class));
        val preAuthorizedCode = sessionTicket.getProperty("preAuthorizedCode", String.class);
        assertNotNull(preAuthorizedCode);
        val preAuthorizationTicket = (TransientSessionTicket) oidcVerifiableCredentialTransactionService.fetchPreAuthorizationCode(preAuthorizedCode);
        assertNotNull(preAuthorizationTicket);
        assertEquals(ticket.getId(), preAuthorizationTicket.getProperty("transactionId", String.class));

        val transactionCode = sessionTicket.getPropertyAsString(
            OidcVerifiableCredentialTransactionService.PROPERTY_TRANSACTION_CODE);
        assertNotNull(transactionCode);
        assertNotEquals(ticket.getId(), transactionCode);
        assertEquals(transactionCode, preAuthorizationTicket.getPropertyAsString(
            OidcVerifiableCredentialTransactionService.PROPERTY_TRANSACTION_CODE));
    }

    @Test
    void verifyIssueWithSingleCredentialConfigId() {
        val registeredService = getOidcRegisteredService();
        val credentialConfigIds = List.of("UniversityDegreeCredential");
        val ticket = oidcVerifiableCredentialTransactionService.issue(registeredService.getClientId(), "casuser", credentialConfigIds);
        assertNotNull(ticket);
        val sessionTicket = (TransientSessionTicket) ticketRegistry.getTicket(ticket.getId());
        assertNotNull(sessionTicket);
        assertEquals(credentialConfigIds, sessionTicket.getProperty("credentialConfigurationIds", List.class));
    }

    @Test
    void verifyIssueWithEmptyCredentialConfigIds() {
        val registeredService = getOidcRegisteredService();
        val ticket = oidcVerifiableCredentialTransactionService.issue(registeredService.getClientId(), "casuser", List.of());
        assertNotNull(ticket);
        val sessionTicket = (TransientSessionTicket) ticketRegistry.getTicket(ticket.getId());
        assertNotNull(sessionTicket);
        assertEquals(List.of(), sessionTicket.getProperty("credentialConfigurationIds", List.class));
    }

    @Test
    void verifyPreAuthorizationCodeIsRedeemedOnlyOnce() {
        val preAuthorizedCode = issuePreAuthorizationCode();
        assertNotNull(oidcVerifiableCredentialTransactionService.consumePreAuthorizationCode(preAuthorizedCode));
        assertNull(oidcVerifiableCredentialTransactionService.consumePreAuthorizationCode(preAuthorizedCode));
        assertNull(oidcVerifiableCredentialTransactionService.fetchPreAuthorizationCode(preAuthorizedCode));
    }

    @Test
    void verifyConcurrentRedemptionsProduceOneWinner() throws Throwable {
        val preAuthorizedCode = issuePreAuthorizationCode();
        val attempts = 16;
        val barrier = new CyclicBarrier(attempts);
        val winners = new AtomicInteger();
        val tasks = IntStream.range(0, attempts)
            .<Callable<Void>>mapToObj(_ -> () -> {
                barrier.await();
                if (oidcVerifiableCredentialTransactionService.consumePreAuthorizationCode(preAuthorizedCode) != null) {
                    winners.incrementAndGet();
                }
                return null;
            })
            .toList();
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (val future : executor.invokeAll(tasks)) {
                future.get();
            }
        }
        assertEquals(1, winners.get());
    }

    @Test
    void verifyConsumeUnknownPreAuthorizationCode() {
        assertNull(oidcVerifiableCredentialTransactionService.consumePreAuthorizationCode("TST-unknown-code"));
    }

    private String issuePreAuthorizationCode() {
        val registeredService = getOidcRegisteredService();
        val ticket = oidcVerifiableCredentialTransactionService.issue(
            registeredService.getClientId(), "casuser", List.of("VerifiableCredential"));
        assertNotNull(ticket);
        val sessionTicket = (TransientSessionTicket) ticketRegistry.getTicket(ticket.getId());
        return Objects.requireNonNull(sessionTicket.getProperty("preAuthorizedCode", String.class));
    }

    @Test
    void verifyFetchValidTicket() {
        val registeredService = getOidcRegisteredService();
        val ticket = oidcVerifiableCredentialTransactionService.issue(registeredService.getClientId(), "casuser", List.of("VerifiableCredential"));
        assertNotNull(ticket);
        val fetched = oidcVerifiableCredentialTransactionService.fetch(ticket.getId());
        assertNotNull(fetched);
        assertEquals(ticket.getId(), fetched.getId());
    }

    @Test
    void verifyFetchUnknownTicket() {
        val fetched = oidcVerifiableCredentialTransactionService.fetch("TST-unknown-ticket-id");
        assertNull(fetched);
    }

    @Test
    void verifyFetchExpiredTicket() {
        val registeredService = getOidcRegisteredService();
        val ticket = oidcVerifiableCredentialTransactionService.issue(registeredService.getClientId(), "casuser", List.of("VerifiableCredential"));
        assertNotNull(ticket);
        val sessionTicket = (TransientSessionTicket) ticketRegistry.getTicket(ticket.getId());
        assertNotNull(sessionTicket);
        sessionTicket.markTicketExpired();
        val fetched = oidcVerifiableCredentialTransactionService.fetch(ticket.getId());
        assertNull(fetched);
    }
}
