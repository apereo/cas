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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcPhoneScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAttributes")
class OidcPhoneScopeAttributeReleasePolicyTests {
    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.core.claims-map.phone_number=cell_phone")
    class ClaimMappingsTests extends AbstractOidcTests {
        @Test
        void verifyMappedToUnknown() throws Throwable {
            val policy = new OidcPhoneScopeAttributeReleasePolicy();
            val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(),
                CollectionUtils.wrap("phone_number", List.of("12134321245")));

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            registeredService.setAttributeReleasePolicy(policy);

            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .service(CoreAuthenticationTestUtils.getService())
                .principal(principal)
                .applicationContext(applicationContext)
                .build();
            val attrs = policy.getAttributes(releasePolicyContext);
            assertTrue(attrs.containsKey("phone_number"));
        }

        @Test
        void verifyMapped() throws Throwable {
            val policy = new OidcPhoneScopeAttributeReleasePolicy();
            val principal = CoreAuthenticationTestUtils.getPrincipal(
                CollectionUtils.wrap("cell_phone", List.of("12134321245")));

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            registeredService.setAttributeReleasePolicy(policy);
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .service(CoreAuthenticationTestUtils.getService())
                .principal(principal)
                .applicationContext(applicationContext)
                .build();
            val attrs = policy.getAttributes(releasePolicyContext);
            assertTrue(attrs.containsKey("phone_number"));
        }
    }

    @Nested
    class DefaultTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val policy = new OidcPhoneScopeAttributeReleasePolicy();
            assertEquals(OidcConstants.StandardScopes.PHONE.getScope(), policy.getScopeType());
            assertNotNull(policy.getAllowedAttributes());
            val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("phone_number_verified", List.of("12134321245"),
                "phone_number", List.of("12134321245")));

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            registeredService.setAttributeReleasePolicy(policy);
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext
                .builder()
                .registeredService(registeredService)
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
            val policy = new OidcPhoneScopeAttributeReleasePolicy();
            val chain = new ChainingAttributeReleasePolicy();
            chain.addPolicies(policy);
            val service = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            service.setAttributeReleasePolicy(chain);
            val serializer = new RegisteredServiceJsonSerializer(applicationContext);
            val json = serializer.toString(service);
            assertNotNull(json);
            assertNotNull(serializer.from(json));
        }
    }
}
