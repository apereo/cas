package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcOpenIdScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcOpenIdScopeAttributeReleasePolicyTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        val policy = new OidcOpenIdScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.OPENID.getScope(), policy.getScopeType());
        assertTrue(policy.getAllowedAttributes().isEmpty());
    }
}
