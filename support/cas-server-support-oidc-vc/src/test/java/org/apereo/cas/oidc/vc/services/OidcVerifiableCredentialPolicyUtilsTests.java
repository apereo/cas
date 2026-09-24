package org.apereo.cas.oidc.vc.services;

import module java.base;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcVerifiableCredentialPolicyUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDC")
class OidcVerifiableCredentialPolicyUtilsTests {
    private static final Set<String> PUBLISHED = Set.of("myorg", "employee", "student");

    @Test
    void verifyNoPolicyAllowsEverything() {
        val registeredService = new OidcRegisteredService();
        assertEquals(PUBLISHED,
            OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds(registeredService, PUBLISHED));
    }

    @Test
    void verifyNonOidcServiceAllowsEverything() {
        assertEquals(PUBLISHED, OidcVerifiableCredentialPolicyUtils
            .resolveAllowedCredentialConfigurationIds(RegisteredServiceTestUtils.getRegisteredService(), PUBLISHED));
    }

    @Test
    void verifyUnknownServiceAllowsEverything() {
        assertEquals(PUBLISHED,
            OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds(null, PUBLISHED));
    }

    @Test
    void verifyPolicyWithoutCredentialTypesAllowsEverything() {
        val registeredService = new OidcRegisteredService();
        registeredService.setVerifiableCredentialsPolicy(new DefaultRegisteredServiceOidcVerifiableCredentialsPolicy());
        assertEquals(PUBLISHED,
            OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds(registeredService, PUBLISHED));
    }

    @Test
    void verifyPolicyNarrowsToItsOwnCredentialTypes() {
        val registeredService = new OidcRegisteredService();
        registeredService.setVerifiableCredentialsPolicy(
            new DefaultRegisteredServiceOidcVerifiableCredentialsPolicy(Set.of("employee")));
        assertEquals(Set.of("employee"),
            OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds(registeredService, PUBLISHED));
    }

    @Test
    void verifyPolicyCannotWidenBeyondWhatTheIssuerPublishes() {
        val registeredService = new OidcRegisteredService();
        registeredService.setVerifiableCredentialsPolicy(
            new DefaultRegisteredServiceOidcVerifiableCredentialsPolicy(Set.of("employee", "nonexistent")));
        assertEquals(Set.of("employee"),
            OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds(registeredService, PUBLISHED));
    }

    @Test
    void verifyPolicyThatMatchesNothingAllowsNothing() {
        val registeredService = new OidcRegisteredService();
        registeredService.setVerifiableCredentialsPolicy(
            new DefaultRegisteredServiceOidcVerifiableCredentialsPolicy(Set.of("nonexistent")));
        assertTrue(OidcVerifiableCredentialPolicyUtils
            .resolveAllowedCredentialConfigurationIds(registeredService, PUBLISHED).isEmpty());
    }
}
