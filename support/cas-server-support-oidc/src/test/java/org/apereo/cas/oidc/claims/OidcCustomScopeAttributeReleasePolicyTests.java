package org.apereo.cas.oidc.claims;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcCustomScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */

@TestPropertySource(
    properties = "cas.authn.oidc.discovery.claims=sub,name,given_name,family_name,middle_name,preferred_username,email,mail,groups")
@Tag("OIDCAttributes")
class OidcCustomScopeAttributeReleasePolicyTests extends AbstractOidcTests {
    @Test
    void verifyOperation() throws Throwable {
        val policy = new OidcCustomScopeAttributeReleasePolicy("groups", CollectionUtils.wrap("groups"));
        assertEquals(OidcConstants.CUSTOM_SCOPE_TYPE, policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());
        val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(),
            CollectionUtils.wrap("groups", List.of("admin", "user")));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val attrs = policy.getAttributes(releasePolicyContext);
        assertTrue(policy.getAllowedAttributes().stream().allMatch(attrs::containsKey));
        val principal2 = CoreAuthenticationTestUtils.getPrincipal(attrs);

        val releasePolicyContext2 = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal2)
            .applicationContext(applicationContext)
            .build();
        val releaseAttrs = policy.getAttributes(releasePolicyContext2);
        assertTrue(policy.getAllowedAttributes().stream().allMatch(releaseAttrs::containsKey));
        assertTrue(policy.getAllowedAttributes().containsAll(policy.determineRequestedAttributeDefinitions(releasePolicyContext2)));
        assertEquals(List.of("admin", "user"), releaseAttrs.get("groups"));
    }

    @Test
    void verifyGroovyMappingInline() throws Throwable {
        ApplicationContextProvider.holdApplicationContext(oidcConfigurationContext.getApplicationContext());
        val policy = new OidcCustomScopeAttributeReleasePolicy("groups", CollectionUtils.wrap("groups"));
        policy.setClaimMappings(Map.of("groups", "groovy { return attributes['groups'] }"));
        val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("groups", List.of("admin", "user")));
        val oidcRegisteredService = getOidcRegisteredService();
        oidcRegisteredService.setAttributeReleasePolicy(policy);
        
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(oidcRegisteredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val attrs = policy.getAttributes(releasePolicyContext);
        assertEquals(List.of("admin", "user"), attrs.get("groups"));
    }

    @Test
    void verifySerialization() {
        val policy = new OidcCustomScopeAttributeReleasePolicy("groups", CollectionUtils.wrap("groups"));
        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicies(policy);
        val service = getOidcRegisteredService();
        service.setAttributeReleasePolicy(chain);
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        var json = serializer.toString(service);
        assertNotNull(json);
        assertNotNull(serializer.from(json));
    }
}
