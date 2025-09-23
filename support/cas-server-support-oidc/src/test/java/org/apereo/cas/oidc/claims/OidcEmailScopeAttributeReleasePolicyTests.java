package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcEmailScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAttributes")
@TestPropertySource(properties = {
    "cas.authn.oidc.core.claims-map.email=mail",
    "cas.authn.oidc.core.claims-map.email_verified=mail_confirmed"
})
class OidcEmailScopeAttributeReleasePolicyTests extends AbstractOidcTests {
    @Test
    void verifyOperation() throws Throwable {
        val policy = new OidcEmailScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.EMAIL.getScope(), policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());
        val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(),
            CollectionUtils.wrap("email", List.of("cas@example.org"),
            "email_verified", List.of("cas@example.org")));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .applicationContext(applicationContext)
            .build();
        val attrs = policy.getAttributes(releasePolicyContext);
        assertTrue(policy.getAllowedAttributes().stream().allMatch(attrs::containsKey));
        assertTrue(policy.determineRequestedAttributeDefinitions(releasePolicyContext).containsAll(policy.getAllowedAttributes()));
    }

    @Test
    void verifyClaimMapOperation() throws Throwable {
        val policy = new OidcEmailScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.EMAIL.getScope(), policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());

        val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("mail", List.of("cas@example.org"),
            "mail_confirmed", List.of("cas@example.org"), "phone", List.of("0000000000")));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .applicationContext(applicationContext)
            .build();
        val attrs = policy.getAttributes(releasePolicyContext);
        assertEquals(List.of("cas@example.org"), attrs.get("email"));
        assertEquals(List.of("cas@example.org"), attrs.get("email_verified"));
        assertFalse(attrs.containsKey("phone"));

        val serviceTicketPrincipal = CoreAuthenticationTestUtils.getPrincipal(attrs);
        val releasePolicyContext2 = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(serviceTicketPrincipal)
            .applicationContext(applicationContext)
            .build();
        val releaseAttrs = policy.getAttributes(releasePolicyContext2);
        assertEquals(List.of("cas@example.org"), releaseAttrs.get("email"));
        assertEquals(List.of("cas@example.org"), releaseAttrs.get("email_verified"));
        assertFalse(releaseAttrs.containsKey("phone"));
    }

    @Test
    void verifySerialization() {
        val policy = new OidcEmailScopeAttributeReleasePolicy();
        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicies(policy);
        val service = getOidcRegisteredService();
        service.setAttributeReleasePolicy(chain);
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val json = serializer.toString(service);
        assertNotNull(json);
        assertNotNull(serializer.from(json));
    }
}
