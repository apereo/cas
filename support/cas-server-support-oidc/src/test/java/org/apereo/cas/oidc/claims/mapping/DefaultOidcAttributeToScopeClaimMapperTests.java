package org.apereo.cas.oidc.claims.mapping;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOidcAttributeToScopeClaimMapperTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
        "cas.authn.oidc.claimsMap.email=mail",
        "cas.authn.oidc.claimsMap.email_verified=mail_confirmed"
})
@Tag("OIDC")
public class DefaultOidcAttributeToScopeClaimMapperTests extends AbstractOidcTests {

    @Test
    public void verifyOperation() {
        val mapper = new DefaultOidcAttributeToScopeClaimMapper(CollectionUtils.wrap("name", "givenName"));
        assertTrue(mapper.containsMappedAttribute("name"));
        assertEquals("givenName", mapper.getMappedAttribute("name"));
    }

    @Test
    public void verifyClaimMapOperation() {
        val policy = new OidcEmailScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.EMAIL.getScope(), policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());

        val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("mail", List.of("cas@example.org"),
                "mail_confirmed", List.of("cas@example.org"), "phone", List.of("0000000000")));
        val attrs = policy.getAttributes(principal,
                CoreAuthenticationTestUtils.getService(),
                CoreAuthenticationTestUtils.getRegisteredService());
        assertEquals(List.of("cas@example.org"), attrs.get("email"));
        assertEquals(List.of("cas@example.org"), attrs.get("email_verified"));
        assertFalse(attrs.containsKey("phone"));

        val serviceTicketPrincipal = CoreAuthenticationTestUtils.getPrincipal(attrs);
        val releaseAttrs = policy.getAttributes(serviceTicketPrincipal,
                CoreAuthenticationTestUtils.getService(),
                CoreAuthenticationTestUtils.getRegisteredService());
        assertEquals(List.of("cas@example.org"), releaseAttrs.get("email"));
        assertEquals(List.of("cas@example.org"), releaseAttrs.get("email_verified"));
        assertFalse(releaseAttrs.containsKey("phone"));
    }
}
