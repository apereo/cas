package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultAttributeToScopeClaimMapperTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAttributes")
class OidcDefaultAttributeToScopeClaimMapperTests {

    @Nested
    class ClaimMappingTests extends AbstractOidcTests {
        @Test
        void verifyOperation() {
            val mapper = new OidcDefaultAttributeToScopeClaimMapper(Map.of());

            val service = getOidcRegisteredService();
            val policy = new OidcProfileScopeAttributeReleasePolicy();
            policy.setClaimMappings(Map.of("name", "givenName"));
            service.setAttributeReleasePolicy(policy);

            assertTrue(mapper.containsMappedAttribute("name", service));
            assertEquals("givenName", mapper.getMappedAttribute("name", service));
        }

        @Test
        void verifyChainOperation() {
            val mapper = new OidcDefaultAttributeToScopeClaimMapper(Map.of());

            val service = getOidcRegisteredService();
            val policy = new OidcProfileScopeAttributeReleasePolicy();
            policy.setClaimMappings(Map.of("name", "givenName"));
            service.setAttributeReleasePolicy(new ChainingAttributeReleasePolicy().addPolicies(policy));

            assertTrue(mapper.containsMappedAttribute("name", service));
            assertEquals("givenName", mapper.getMappedAttribute("name", service));
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.oidc.core.claims-map.email=mail",
        "cas.authn.oidc.core.claims-map.email_verified=mail_confirmed"
    })
    @Nested
    class DefaultTests extends AbstractOidcTests {
        @Test
        void verifyValueTypes() {
            val oidcRegisteredService = getOidcRegisteredService();
            val mapper = new OidcDefaultAttributeToScopeClaimMapper(
                CollectionUtils.wrap("active1", "status1", "active2", "status2",
                    "active3", "status3"));
            val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
                CollectionUtils.wrap("status1", "true",
                    "status2", false, "status3", 1));
            var value = mapper.mapClaim("active1", oidcRegisteredService, principal, null).getFirst();
            assertInstanceOf(Boolean.class, value);
            value = mapper.mapClaim("active2", oidcRegisteredService, principal, null).getFirst();
            assertInstanceOf(Boolean.class, value);
            value = mapper.mapClaim("active3", oidcRegisteredService, principal, null).getFirst();
            assertInstanceOf(Number.class, value);
        }

        @Test
        void verifyOperation() {
            val mapper = new OidcDefaultAttributeToScopeClaimMapper(CollectionUtils.wrap("name", "givenName"));
            val oidcRegisteredService = getOidcRegisteredService();
            assertTrue(mapper.containsMappedAttribute("name", oidcRegisteredService));
            assertEquals("givenName", mapper.getMappedAttribute("name", oidcRegisteredService));
        }

        @Test
        void verifyClaimMapOperation() throws Throwable {
            val policy = new OidcEmailScopeAttributeReleasePolicy();
            assertEquals(OidcConstants.StandardScopes.EMAIL.getScope(), policy.getScopeType());
            assertNotNull(policy.getAllowedAttributes());
            val principal = CoreAuthenticationTestUtils.getPrincipal(
                UUID.randomUUID().toString(),
                CollectionUtils.wrap("mail", List.of("cas@example.org"),
                    "mail_confirmed", List.of("cas@example.org")));

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
    }
}
