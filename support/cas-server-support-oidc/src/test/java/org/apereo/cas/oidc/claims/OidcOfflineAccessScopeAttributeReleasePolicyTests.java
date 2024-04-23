package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.OidcConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcOfflineAccessScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDC")
public class OidcOfflineAccessScopeAttributeReleasePolicyTests {
    @Test
    void verifyOperation() throws Throwable {
        val policy = new OidcOfflineAccessScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.OFFLINE_ACCESS.getScope(), policy.getScopeType());
        assertTrue(policy.getAllowedAttributes().isEmpty());
    }
}
