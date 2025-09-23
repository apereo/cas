package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
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
 * This is {@link OidcScopeFreeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAttributes")
class OidcScopeFreeAttributeReleasePolicyTests extends AbstractOidcTests {

    @Test
    void verifyOperation() throws Throwable {
        val policy = new OidcScopeFreeAttributeReleasePolicy(List.of("family_name", "food"));
        assertEquals(OidcScopeFreeAttributeReleasePolicy.ANY_SCOPE, policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());
        assertFalse(policy.claimsMustBeDefinedViaDiscovery());

        val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(),
            CollectionUtils.wrap("name", List.of("cas"),
                "profile", List.of("test"),
                "food", List.of("salad"),
                "family_name", List.of("johnson")));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .applicationContext(oidcConfigurationContext.getApplicationContext())
            .build();
        val attrs = policy.getAttributes(releasePolicyContext);
        assertEquals(2, attrs.size());
        assertTrue(policy.getAllowedAttributes().containsAll(attrs.keySet()));
    }

    @Test
    void verifySerialization() {
        val policy = new OidcScopeFreeAttributeReleasePolicy(List.of("family_name", "food"));
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
