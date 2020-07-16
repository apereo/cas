package org.apereo.cas.authentication;

import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.validation.Assertion;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAuthenticationAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Attributes")
public class DefaultAuthenticationAttributeReleasePolicyTests {
    @Test
    public void verifyNoRelease() {
        val policy = new DefaultAuthenticationAttributeReleasePolicy("authnContext");
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val attrPolicy = new ReturnAllowedAttributeReleasePolicy();
        attrPolicy.setAuthorizedToReleaseAuthenticationAttributes(false);
        when(service.getAttributeReleasePolicy()).thenReturn(attrPolicy);
        assertTrue(policy.getAuthenticationAttributesForRelease(CoreAuthenticationTestUtils.getAuthentication(),
            mock(Assertion.class), Map.of(), service).isEmpty());
    }

    @Test
    public void verifyOnlyRelease() {
        val policy = new DefaultAuthenticationAttributeReleasePolicy(Set.of("cn"), Set.of(), "authnContext");
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val attrPolicy = new ReturnAllowedAttributeReleasePolicy();
        when(service.getAttributeReleasePolicy()).thenReturn(attrPolicy);
        val results = policy.getAuthenticationAttributesForRelease(
            CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal(),
                Map.of("cn", List.of("common-name"), "givenName", List.of("given-name"))),
            mock(Assertion.class), Map.of("authnContext", List.of("mfa-something")), service);
        assertEquals(5, results.size());
        assertTrue(results.containsKey("cn"));
        assertTrue(results.containsKey("authnContext"));
    }

}
