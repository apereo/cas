package org.apereo.cas.oidc.services;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcPhoneScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcScopeFreeAttributeReleasePolicy;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceChainingAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcServiceRegistryListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCServices")
@TestPropertySource(properties =
    "cas.authn.oidc.core.user-defined-scopes.SomeCustomScope=name"
)
class OidcServiceRegistryListenerTests extends AbstractOidcTests {

    @Test
    void verifyMatchingScopeWithPolicyAsChain() {
        var service = getOidcRegisteredService();
        val initialPolicy = new OidcPhoneScopeAttributeReleasePolicy();
        initialPolicy.setClaimMappings(Map.of("phone", "my_phone"));
        val chainingPolicy = new ChainingAttributeReleasePolicy();
        chainingPolicy.addPolicies(initialPolicy);
        service.setAttributeReleasePolicy(chainingPolicy);
        val scopes = service.getScopes();
        scopes.add(OidcConstants.StandardScopes.PHONE.getScope());
        scopes.add(OidcConstants.StandardScopes.PROFILE.getScope());
        service = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        val policy = service.getAttributeReleasePolicy();
        assertInstanceOf(RegisteredServiceChainingAttributeReleasePolicy.class, policy);
        val chain = (RegisteredServiceChainingAttributeReleasePolicy) policy;
        assertEquals(3, chain.size());
    }

    @Test
    void verifyMatchingScopeWithPolicy() {
        var service = getOidcRegisteredService();
        val initialPolicy = new OidcPhoneScopeAttributeReleasePolicy();
        initialPolicy.setClaimMappings(Map.of("phone", "my_phone"));
        service.setAttributeReleasePolicy(initialPolicy);
        val scopes = service.getScopes();
        scopes.add(OidcConstants.StandardScopes.PHONE.getScope());
        scopes.add(OidcConstants.StandardScopes.PROFILE.getScope());
        service = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        val policy = service.getAttributeReleasePolicy();
        assertInstanceOf(RegisteredServiceChainingAttributeReleasePolicy.class, policy);
        val chain = (RegisteredServiceChainingAttributeReleasePolicy) policy;
        assertEquals(3, chain.size());
    }

    @Test
    void verifyOperationRecon() {
        var service = getOidcRegisteredService();
        val scopes = service.getScopes();
        scopes.add(OidcConstants.StandardScopes.ADDRESS.getScope());
        scopes.add(OidcConstants.StandardScopes.EMAIL.getScope());
        scopes.add(OidcConstants.StandardScopes.OFFLINE_ACCESS.getScope());
        scopes.add(OidcConstants.StandardScopes.OPENID.getScope());
        scopes.add(OidcConstants.StandardScopes.PHONE.getScope());
        scopes.add(OidcConstants.StandardScopes.PROFILE.getScope());
        scopes.add("SomeCustomScope");
        service = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        val policy = service.getAttributeReleasePolicy();
        assertInstanceOf(RegisteredServiceChainingAttributeReleasePolicy.class, policy);
        val chain = (RegisteredServiceChainingAttributeReleasePolicy) policy;
        assertEquals(5, chain.size());
    }

    @Test
    void verifyCustomScope() {
        var service = getOidcRegisteredService();
        service.getScopes().clear();
        val scopes = service.getScopes();
        scopes.addAll(List.of("cn", "mail"));
        service = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        val policy = service.getAttributeReleasePolicy();
        assertInstanceOf(OidcCustomScopeAttributeReleasePolicy.class, policy);
        val custom = (OidcCustomScopeAttributeReleasePolicy) policy;
        assertTrue(scopes.containsAll(custom.getAllowedAttributes()));
    }

    @Test
    void verifyScopeFreeAttributeRelease() {
        var service = getOidcRegisteredService();
        service.getScopes().clear();

        val scopes = service.getScopes();
        scopes.add(OidcConstants.StandardScopes.OPENID.getScope());
        service.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

        service = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        val policy = service.getAttributeReleasePolicy();
        assertFalse(policy instanceof RegisteredServiceChainingAttributeReleasePolicy);
        assertInstanceOf(ReturnAllAttributeReleasePolicy.class, policy);
    }

    @Test
    void verifyOperationEmptyScopes() {
        var service = getOidcRegisteredService();
        service.getScopes().clear();
        val processed = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        assertEquals(service.getAttributeReleasePolicy(), processed.getAttributeReleasePolicy());
    }

    @Test
    void verifyOperationReconAsChain() {
        var service = getOidcRegisteredService();
        service.getScopes().clear();
        service.getScopes().add(OidcConstants.StandardScopes.OPENID.getScope());
        service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(CollectionUtils.wrapList("cn")));

        service = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        val policy = service.getAttributeReleasePolicy();
        assertFalse(policy instanceof RegisteredServiceChainingAttributeReleasePolicy);
        assertInstanceOf(ReturnAllowedAttributeReleasePolicy.class, policy);
    }

    @Test
    void verifyReleasePolicyStartingWithChain() {
        var service = getOidcRegisteredService();
        service.getScopes().clear();
        service.getScopes().add(OidcConstants.StandardScopes.OPENID.getScope());

        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicies(new ReturnAllowedAttributeReleasePolicy(CollectionUtils.wrapList("cn")));
        service.setAttributeReleasePolicy(chain);

        service = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        val policy = service.getAttributeReleasePolicy();
        assertFalse(policy instanceof RegisteredServiceChainingAttributeReleasePolicy);
        assertInstanceOf(ReturnAllowedAttributeReleasePolicy.class, policy);
    }

    @Test
    void verifyScopeWithPolicyAsChainWithScopeFreePolicy() {
        var service = getOidcRegisteredService();
        val chainingPolicy = new ChainingAttributeReleasePolicy();
        chainingPolicy.addPolicies(new OidcScopeFreeAttributeReleasePolicy(List.of("system_id")));
        chainingPolicy.addPolicies(new OidcScopeFreeAttributeReleasePolicy(List.of("food_pref")));
        service.setAttributeReleasePolicy(chainingPolicy);
        val scopes = service.getScopes();
        scopes.add(OidcConstants.StandardScopes.PHONE.getScope());
        scopes.add(OidcConstants.StandardScopes.PROFILE.getScope());
        service = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        val policy = service.getAttributeReleasePolicy();
        assertInstanceOf(RegisteredServiceChainingAttributeReleasePolicy.class, policy);
        val chain = (RegisteredServiceChainingAttributeReleasePolicy) policy;
        assertEquals(5, chain.size());
    }

}
