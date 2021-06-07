package org.apereo.cas.oidc.services;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcServicesManagerRegisteredServiceLocatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator oidcServicesManagerRegisteredServiceLocator;

    @BeforeEach
    public void setup() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyOperation() {
        assertNotNull(oidcServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE + 1, oidcServicesManagerRegisteredServiceLocator.getOrder());
        val oidcClientId = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(oidcClientId);
        val svc = webApplicationServiceFactory.createService(
            String.format("https://oauth.example.org/whatever?%s=%s", OAuth20Constants.CLIENT_ID, oidcClientId));
        val result = oidcServicesManagerRegisteredServiceLocator.locate(List.of(service), svc);
        assertNotNull(result);
    }

    @Test
    public void verifyNoMatch() {
        assertNotNull(oidcServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE + 1, oidcServicesManagerRegisteredServiceLocator.getOrder());
        val oidcClientId = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(oidcClientId);
        val svc = webApplicationServiceFactory.createService(
                String.format("https://oauth.example.org/whatever?%s=%s", OAuth20Constants.CLIENT_ID, "nomatch"));
        val result = oidcServicesManagerRegisteredServiceLocator.locate(List.of(service), svc);
        assertNull(result);
    }

    @Test
    public void verifyNoClientId() {
        assertNotNull(oidcServicesManagerRegisteredServiceLocator);
        val oidcClientId = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(oidcClientId);
        val svc = webApplicationServiceFactory.createService("https://oauth.example.org/whatever");
        val result = oidcServicesManagerRegisteredServiceLocator.locate(List.of(service), svc);
        assertNull(result);
    }

    @Test
    public void verifyNoOidcCandidate() {
        assertNotNull(oidcServicesManagerRegisteredServiceLocator);
        val oidcClientId = UUID.randomUUID().toString();
        val service = RegisteredServiceTestUtils.getRegisteredService("https://notooidc.example.org/whatever");
        val svc = webApplicationServiceFactory.createService(
                String.format("https://oauth.example.org/whatever?%s=%s", OAuth20Constants.CLIENT_ID, oidcClientId));
        val result = oidcServicesManagerRegisteredServiceLocator.locate(List.of(service), svc);
        assertNull(result);
    }

    @Test
    public void verifyReverseOperation() {
        val service1 = RegisteredServiceTestUtils.getRegisteredService(".+");
        service1.setEvaluationOrder(5);

        val oidcClientId = UUID.randomUUID().toString();
        val service2 = getOidcRegisteredService(oidcClientId, ".+", false, false);
        service2.setEvaluationOrder(10);

        val oauthClientId = UUID.randomUUID().toString();
        val service3 = getOAuthRegisteredService(oauthClientId, ".+");
        service3.setEvaluationOrder(15);

        servicesManager.save(service1, service2, service3);

        var svc = webApplicationServiceFactory.createService(
            String.format("https://app.example.org/whatever?%s=%s", OAuth20Constants.CLIENT_ID, oidcClientId));
        var result = servicesManager.findServiceBy(svc);
        assertTrue(result instanceof OidcRegisteredService);

        svc = webApplicationServiceFactory.createService("https://app.example.org/whatever?hello=world");
        result = servicesManager.findServiceBy(svc);
        assertTrue(result instanceof RegexRegisteredService);
    }

}
