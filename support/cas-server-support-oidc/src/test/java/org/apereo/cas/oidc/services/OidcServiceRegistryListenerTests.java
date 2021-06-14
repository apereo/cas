package org.apereo.cas.oidc.services;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcServiceRegistryListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
@TestPropertySource(properties =
    "cas.authn.oidc.core.user-defined-scopes.SomeCustomScope=name"
)
public class OidcServiceRegistryListenerTests extends AbstractOidcTests {

    @Test
    public void verifyOperationRecon() {
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
        assertTrue(policy instanceof ChainingAttributeReleasePolicy);
        val chain = (ChainingAttributeReleasePolicy) policy;
        assertEquals(5, chain.size());
    }

    @Test
    public void verifyScopeFreeAttributeRelease() {
        var service = getOidcRegisteredService();
        service.getScopes().clear();
        
        val scopes = service.getScopes();
        scopes.add(OidcConstants.StandardScopes.OPENID.getScope());
        service.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        
        service = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        val policy = service.getAttributeReleasePolicy();
        assertTrue(policy instanceof ChainingAttributeReleasePolicy);
        val chain = (ChainingAttributeReleasePolicy) policy;
        assertEquals(1, chain.size());
        assertTrue(chain.getPolicies().get(0) instanceof ReturnAllAttributeReleasePolicy);
    }

    @Test
    public void verifyOperationEmptyScopes() {
        var service = getOidcRegisteredService();
        service.getScopes().clear();
        val processed = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        assertEquals(service.getAttributeReleasePolicy(), processed.getAttributeReleasePolicy());
    }

    @Test
    public void verifyUnknownScope() {
        var service = getOidcRegisteredService();
        val scopes = service.getScopes();
        service.getScopes().clear();
        scopes.add("unknown");
        service = (OidcRegisteredService) oidcServiceRegistryListener.postLoad(service);
        val policy = service.getAttributeReleasePolicy();
        assertTrue(policy instanceof DenyAllAttributeReleasePolicy);
    }


}
