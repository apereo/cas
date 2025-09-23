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
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcProfileScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAttributes")
class OidcProfileScopeAttributeReleasePolicyTests extends AbstractOidcTests {

    @Test
    void verifyOperation() throws Throwable {
        val policy = new OidcProfileScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.PROFILE.getScope(), policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());
        val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(),
            CollectionUtils.wrap("name", List.of("cas"),
                "profile", List.of("test"),
                "preferred_username", List.of("casuser"),
                "family_name", List.of("given_name")));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .applicationContext(oidcConfigurationContext.getApplicationContext())
            .build();
        val attrs = policy.getAttributes(releasePolicyContext);
        assertTrue(policy.getAllowedAttributes().containsAll(attrs.keySet()));
        assertTrue(policy.determineRequestedAttributeDefinitions(releasePolicyContext).containsAll(policy.getAllowedAttributes()));
    }

    @Test
    void verifySerialization() {
        val policy = new OidcProfileScopeAttributeReleasePolicy();
        policy.setAllowedAttributes(CollectionUtils.wrapList("name", "gender"));
        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicies(policy);
        val service = getOidcRegisteredService();
        service.setAttributeReleasePolicy(chain);
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val json = serializer.toString(service);
        assertNotNull(json);
        val read = serializer.from(json);
        assertEquals(read, service);
    }
}
