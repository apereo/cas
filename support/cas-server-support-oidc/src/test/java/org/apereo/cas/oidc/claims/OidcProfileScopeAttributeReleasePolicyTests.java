package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcProfileScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcProfileScopeAttributeReleasePolicyTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        val policy = new OidcProfileScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.PROFILE.getScope(), policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());
        val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("name", List.of("cas"),
            "profile", List.of("test"),
            "preferred_username", List.of("casuser"),
            "family_name", List.of("given_name")));
        val attrs = policy.getAttributes(principal,
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(policy.getAllowedAttributes().containsAll(attrs.keySet()));
        assertTrue(policy.determineRequestedAttributeDefinitions(
            principal,
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService()
        ).containsAll(policy.getAllowedAttributes()));
    }

    @Test
    public void verifySerialization() {
        val policy = new OidcProfileScopeAttributeReleasePolicy();
        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicy(policy);
        val service = getOidcRegisteredService();
        service.setAttributeReleasePolicy(chain);
        val serializer = new RegisteredServiceJsonSerializer();
        val json = serializer.toString(service);
        assertNotNull(json);
        assertNotNull(serializer.from(json));
    }
}
