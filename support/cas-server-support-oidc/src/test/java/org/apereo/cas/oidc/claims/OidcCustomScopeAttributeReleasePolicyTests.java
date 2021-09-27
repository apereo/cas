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
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcCustomScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */

@TestPropertySource(
    properties = "cas.authn.oidc.discovery.claims=sub,name,given_name,family_name,middle_name,preferred_username,email,mail,groups")
@Tag("OIDC")
public class OidcCustomScopeAttributeReleasePolicyTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        val policy = new OidcCustomScopeAttributeReleasePolicy("groups", CollectionUtils.wrap("groups"));
        assertEquals(OidcConstants.CUSTOM_SCOPE_TYPE, policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());
        val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("groups", List.of("admin", "user")));
        val attrs = policy.getAttributes(principal,
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(policy.getAllowedAttributes().stream().allMatch(attrs::containsKey));
        val principal2 = CoreAuthenticationTestUtils.getPrincipal(attrs);
        val releaseAttrs = policy.getAttributes(principal2,
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(policy.getAllowedAttributes().stream().allMatch(releaseAttrs::containsKey));
        assertTrue(policy.getAllowedAttributes().containsAll(policy.determineRequestedAttributeDefinitions(
            principal2,
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService()
        )));
        assertEquals(releaseAttrs.get("groups"), List.of("admin", "user"));
    }

    @Test
    public void verifySerialization() {
        val policy = new OidcCustomScopeAttributeReleasePolicy("groups", CollectionUtils.wrap("groups"));
        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicy(policy);
        val service = getOidcRegisteredService();
        service.setAttributeReleasePolicy(chain);
        val serializer = new RegisteredServiceJsonSerializer();
        var json = serializer.toString(service);
        assertNotNull(json);
        assertNotNull(serializer.from(json));
    }
}
