package org.apereo.cas.oidc.vc.offer.web;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialOfferService;
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
 * This is {@link OidcVerifiableCredentialDefaultOfferServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDC")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
class OidcVerifiableCredentialDefaultOfferServiceTests extends AbstractOidcTests {

    @Autowired
    @Qualifier(OidcVerifiableCredentialOfferService.BEAN_NAME)
    private OidcVerifiableCredentialOfferService oidcVerifiableCredentialOfferService;

    @Autowired
    @Qualifier(OidcVerifiableCredentialTransactionService.BEAN_NAME)
    private OidcVerifiableCredentialTransactionService oidcVerifiableCredentialTransactionService;

    @Test
    void verifyCreateOffer() {
        val credentialConfigIds = List.of("UniversityDegreeCredential");
        val offer = oidcVerifiableCredentialOfferService.create("casuser", credentialConfigIds);
        assertNotNull(offer);
        assertEquals(casProperties.getAuthn().getOidc().getCore().getIssuer(), offer.getCredentialIssuer());
        assertEquals(credentialConfigIds, offer.getCredentialConfigurationIds());
        assertNotNull(offer.getGrants());
        val grant = offer.getGrants().getPreAuthorizedCodeGrant();
        assertNotNull(grant);
        assertNotNull(grant.getTxCode());
        assertTrue(grant.getTxCode().startsWith(TransientSessionTicket.PREFIX));
        assertNotNull(grant.getPreAuthorizedCode());
        assertNotNull(grant.getIssuerState());
    }

    @Test
    void verifyCreateOfferWithMultipleCredentialConfigs() {
        val credentialConfigIds = List.of("UniversityDegreeCredential", "DriverLicenseCredential");
        val offer = oidcVerifiableCredentialOfferService.create("casuser", credentialConfigIds);
        assertNotNull(offer);
        assertEquals(credentialConfigIds, offer.getCredentialConfigurationIds());
    }

    @Test
    void verifyFetchOffer() {
        val ticket = oidcVerifiableCredentialTransactionService.issue("casuser", List.of("VerifiableCredential"));
        assertNotNull(ticket);
        val offer = oidcVerifiableCredentialOfferService.fetch(ticket.getId());
        assertNotNull(offer);
        assertEquals(casProperties.getAuthn().getOidc().getCore().getIssuer(), offer.getCredentialIssuer());
        assertEquals(List.of("VerifiableCredential"), offer.getCredentialConfigurationIds());
        val grant = offer.getGrants().getPreAuthorizedCodeGrant();
        assertNotNull(grant);
        assertEquals(ticket.getId(), grant.getTxCode());
        assertNotNull(grant.getPreAuthorizedCode());
        assertNotNull(grant.getIssuerState());
    }

    @Test
    void verifyFetchOfferForUnknownTransaction() {
        assertThrows(IllegalArgumentException.class,
            () -> oidcVerifiableCredentialOfferService.fetch("TST-unknown-id"));
    }
}
