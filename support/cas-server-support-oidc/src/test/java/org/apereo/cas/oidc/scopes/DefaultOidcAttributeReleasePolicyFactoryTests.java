package org.apereo.cas.oidc.scopes;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcScopeFreeAttributeReleasePolicy;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOidcAttributeReleasePolicyFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDCAttributes")
class DefaultOidcAttributeReleasePolicyFactoryTests extends AbstractOidcTests {
    @Autowired
    @Qualifier(OidcAttributeReleasePolicyFactory.BEAN_NAME)
    private OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory;

    @Test
    void verifyOperation() {
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.EMAIL));
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.ADDRESS));
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.PHONE));
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.ADDRESS));
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.OPENID));
        assertNotNull(oidcAttributeReleasePolicyFactory.get(OidcConstants.StandardScopes.OFFLINE_ACCESS));
    }

    @Test
    void verifyEffectivePolicies() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());

        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicies(new OidcCustomScopeAttributeReleasePolicy("eduPerson", List.of("uid")));
        chain.addPolicies(
            new OidcScopeFreeAttributeReleasePolicy(List.of("sys_user")),
            new OidcScopeFreeAttributeReleasePolicy(List.of("dev_user")),
            new OidcScopeFreeAttributeReleasePolicy(List.of("adm_user")));
        registeredService.setAttributeReleasePolicy(chain);
        
        val policies = oidcAttributeReleasePolicyFactory.resolvePolicies(registeredService);
        assertEquals(10, policies.size());
        assertTrue(policies.containsKey("eduPerson"));
        assertTrue(policies.containsKey(OidcConstants.StandardScopes.EMAIL.getScope()));
        assertTrue(policies.containsKey(OidcConstants.StandardScopes.PROFILE.getScope()));
        assertTrue(policies.containsKey(OidcConstants.StandardScopes.OPENID.getScope()));
        assertTrue(policies.containsKey(OidcConstants.StandardScopes.ADDRESS.getScope()));
        val count = policies.values().stream().filter(OidcScopeFreeAttributeReleasePolicy.class::isInstance).count();
        assertEquals(3, count);
    }

    @Test
    void verifyEffectivePoliciesWithChain() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        val chain = new ChainingAttributeReleasePolicy()
            .addPolicies(new OidcCustomScopeAttributeReleasePolicy("eduPerson", List.of("uid")));
        registeredService.setAttributeReleasePolicy(chain);
        val policies = oidcAttributeReleasePolicyFactory.resolvePolicies(registeredService);
        assertTrue(policies.containsKey("eduPerson"));
        assertTrue(policies.containsKey(OidcConstants.StandardScopes.EMAIL.getScope()));
        assertTrue(policies.containsKey(OidcConstants.StandardScopes.PROFILE.getScope()));
    }
}
