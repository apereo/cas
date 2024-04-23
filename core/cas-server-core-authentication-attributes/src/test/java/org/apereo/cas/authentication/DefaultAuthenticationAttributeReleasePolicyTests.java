package org.apereo.cas.authentication;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.validation.Assertion;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAuthenticationAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Attributes")
class DefaultAuthenticationAttributeReleasePolicyTests {
    @Test
    void verifyNoRelease() throws Throwable {
        val policy = new DefaultAuthenticationAttributeReleasePolicy("authnContext");
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val attrPolicy = new ReturnAllowedAttributeReleasePolicy();
        attrPolicy.setAuthorizedToReleaseAuthenticationAttributes(false);
        when(service.getAttributeReleasePolicy()).thenReturn(attrPolicy);
        assertTrue(policy.getAuthenticationAttributesForRelease(CoreAuthenticationTestUtils.getAuthentication(),
            mock(Assertion.class), Map.of(), service).isEmpty());
        assertTrue(policy.getAuthenticationAttributesForRelease(CoreAuthenticationTestUtils.getAuthentication(), service).isEmpty());
    }

    @Test
    void verifyNoReleaseCredential() throws Throwable {
        val policy = new DefaultAuthenticationAttributeReleasePolicy("authnContext");
        policy.getOnlyReleaseAttributes().add(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val attrPolicy = new ReturnAllowedAttributeReleasePolicy();
        attrPolicy.setAuthorizedToReleaseCredentialPassword(false);
        when(service.getAttributeReleasePolicy()).thenReturn(attrPolicy);
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            Map.of(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, List.of("Password")));
        assertFalse(policy.getAuthenticationAttributesForRelease(authentication,
            mock(Assertion.class), Map.of(), service).isEmpty());
    }

    @Test
    void verifyReleaseCredentialAllowed() throws Throwable {
        val policy = new DefaultAuthenticationAttributeReleasePolicy("authnContext");
        policy.getOnlyReleaseAttributes().add(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val attrPolicy = new ReturnAllowedAttributeReleasePolicy();
        attrPolicy.setAuthorizedToReleaseCredentialPassword(true);
        when(service.getAttributeReleasePolicy()).thenReturn(attrPolicy);
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            Map.of(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, List.of("Password")));
        assertFalse(policy.getAuthenticationAttributesForRelease(authentication,
            mock(Assertion.class), Map.of(), service).isEmpty());
    }

    @Test
    void verifyOnlyRelease() throws Throwable {
        val policy = new DefaultAuthenticationAttributeReleasePolicy(Set.of("cn", "authnContext"),
            Set.of(), "authnContext");
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val attrPolicy = new ReturnAllowedAttributeReleasePolicy();
        when(service.getAttributeReleasePolicy()).thenReturn(attrPolicy);
        val results = policy.getAuthenticationAttributesForRelease(
            CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal(),
                Map.of("cn", List.of("common-name"), "givenName", List.of("given-name"))),
            mock(Assertion.class), Map.of("authnContext", List.of("mfa-something")), service);
        assertEquals(2, results.size());
        assertTrue(results.containsKey("cn"));
        assertTrue(results.containsKey("authnContext"));
    }

    @Test
    void verifyReleaseAll() throws Throwable {
        val policy = new DefaultAuthenticationAttributeReleasePolicy(Set.of(), Set.of(), "authnContext");
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val attrPolicy = new ReturnAllowedAttributeReleasePolicy();
        when(service.getAttributeReleasePolicy()).thenReturn(attrPolicy);
        val results = policy.getAuthenticationAttributesForRelease(
            CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal(),
                Map.of("cn", List.of("common-name"), "givenName", List.of("given-name"))),
            mock(Assertion.class), Map.of("authnContext", List.of("mfa-something")), service);
        assertEquals(6, results.size());
        assertTrue(results.containsKey("cn"));
        assertTrue(results.containsKey("givenName"));
        assertTrue(results.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE));
        assertTrue(results.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN));
        assertTrue(results.containsKey(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
        assertTrue(results.containsKey("authnContext"));
    }

    @Test
    void verifyReleaseProxyGrantingTicket() throws Throwable {
        val policy = new DefaultAuthenticationAttributeReleasePolicy(
            Set.of(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET),
            Set.of(), "authnContext");
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val attrPolicy = new ReturnAllowedAttributeReleasePolicy();
        when(service.getAttributeReleasePolicy()).thenReturn(attrPolicy);
        val authentication = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal());
        var results = policy.getAuthenticationAttributesForRelease(authentication, mock(Assertion.class), Map.of(), service);
        assertTrue(results.isEmpty());
        attrPolicy.setAuthorizedToReleaseProxyGrantingTicket(true);
        val pgt = mock(Ticket.class);
        val pgtId = UUID.randomUUID().toString();
        when(pgt.getId()).thenReturn(pgtId);
        results = policy.getAuthenticationAttributesForRelease(
            authentication, mock(Assertion.class),
            Map.of(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, pgt), service);
        assertEquals(1, results.size());
        assertTrue(results.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET));
    }

}
