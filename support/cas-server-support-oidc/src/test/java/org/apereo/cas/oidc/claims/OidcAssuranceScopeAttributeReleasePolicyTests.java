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
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAssuranceScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.oidc.discovery.scopes=openid,profile,assurance",
    "cas.authn.oidc.discovery.claims=sub,name,nationalities,birth_family_name,title,place_of_birth"
})
@Tag("OIDCAttributes")
class OidcAssuranceScopeAttributeReleasePolicyTests extends AbstractOidcTests {
    @Test
    void verifyOperation() throws Throwable {
        val policy = new OidcAssuranceScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.ASSURANCE.getScope(), policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());
        val attributes = CollectionUtils.<String, List<Object>>wrap("birth_family_name", List.of("Johnson"),
            "title", List.of("MRS"), "place_of_birth", List.of("London"));
        val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(), attributes);

        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
        registeredService.setAttributeReleasePolicy(policy);
        servicesManager.save(registeredService);
        
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .applicationContext(applicationContext)
            .build();
        val attrs = policy.getAttributes(releasePolicyContext);
        assertTrue(principal.getAttributes().keySet().stream().allMatch(attrs::containsKey));
    }

    @Test
    void verifySerialization() {
        val policy = new OidcAssuranceScopeAttributeReleasePolicy();
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
