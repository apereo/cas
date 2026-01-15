package org.apereo.cas.oidc.claims;

import module java.base;
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
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAddressScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAttributes")
class OidcAddressScopeAttributeReleasePolicyTests extends AbstractOidcTests {
    @Test
    void verifyOperation() throws Throwable {
        val policy = new OidcAddressScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.ADDRESS.getScope(), policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());
        val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(),
            CollectionUtils.wrap("name", List.of("cas"), "address", List.of("Main St")));
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
    void verifySerialization() {
        val policy = new OidcAddressScopeAttributeReleasePolicy();
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
