package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML")
public class SamlIdPServicesManagerRegisteredServiceLocatorTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlIdPServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator samlIdPServicesManagerRegisteredServiceLocator;

    @BeforeEach
    public void setup() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyOperation() {
        assertNotNull(samlIdPServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE, samlIdPServicesManagerRegisteredServiceLocator.getOrder());
        val service1 = RegisteredServiceTestUtils.getRegisteredService(".+");
        service1.setEvaluationOrder(10);

        val service2 = getSamlRegisteredServiceFor(false, false, false, ".+");
        service2.setEvaluationOrder(9);

        val candidateServices = CollectionUtils.wrapList(service1, service2);
        Collections.sort(candidateServices);

        val result = samlIdPServicesManagerRegisteredServiceLocator.locate(
            (List) candidateServices,
            webApplicationServiceFactory.createService("https://sp.testshib.org/shibboleth-sp"),
            r -> r.matches("https://sp.testshib.org/shibboleth-sp"));
        assertNotNull(result);
    }

    @Test
    public void verifyReverseOperation() {
        val service1 = RegisteredServiceTestUtils.getRegisteredService(".+");
        service1.setEvaluationOrder(9);

        val service2 = getSamlRegisteredServiceFor(false, false, false, ".+");
        service2.setEvaluationOrder(10);

        servicesManager.save(service1, service2);
        val service = webApplicationServiceFactory.createService("https://sp.testshib.org/shibboleth-sp");
        val result = servicesManager.findServiceBy(service);
        assertNotNull(result);
        assertTrue(result instanceof SamlRegisteredService);
    }

    /**
     * serviceLocator should not trigger metadata lookups when requested entityID does not match pattern for service in question.
     *
     * This test verifies that, in the case of one service entry that does not match the requested entityID, no
     * metadata lookups are performed.
     *
     * @author Hayden Sartoris
     */
    @Test
    public void verifyEntityIDFilter() {
        SamlRegisteredServiceCachingMetadataResolver resolver = new BrokenMetadataResolver();
        SamlIdPServicesManagerRegisteredServiceLocator locator = new SamlIdPServicesManagerRegisteredServiceLocator(resolver);

        val service1 = RegisteredServiceTestUtils.getRegisteredService("urn:abc:def.+");
        service1.setEvaluationOrder(9);
        val entityID = "https://sp.testshib.org/shibboleth-sp";
        val service = webApplicationServiceFactory.createService(entityID);

        locator.locate(List.of(service1), service, r -> r.matches(entityID));
    }

    private static class BrokenMetadataResolver implements SamlRegisteredServiceCachingMetadataResolver {
        public MetadataResolver resolve(SamlRegisteredService service, CriteriaSet criteriaSet) {
            throw new IllegalStateException("This method shouldn't have been called");
        }

        public void invalidate() {
        }

        public void invalidate(SamlRegisteredService s, CriteriaSet c) {
        }
    }
}
