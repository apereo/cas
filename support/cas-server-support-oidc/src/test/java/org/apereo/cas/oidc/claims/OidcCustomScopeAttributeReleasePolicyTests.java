package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is {@link OidcCustomScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */

@TestPropertySource(
    properties = "cas.authn.oidc.claims=sub,name,given_name,family_name,middle_name,preferred_username,email,mail,groups")
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
        assertEquals(releaseAttrs.get("groups"), List.of("admin", "user"));
    }
}
