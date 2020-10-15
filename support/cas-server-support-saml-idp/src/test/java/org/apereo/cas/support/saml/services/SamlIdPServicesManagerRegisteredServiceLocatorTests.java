package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
     * This test first verifies that, in the case of one service entry that does not match the requested entityID, no
     * metadata lookups are performed. It then verifies that, in the case of two service entries, one matching the
     * requested entityID, exactly one metadata lookup is performed.
     *
     * @author Hayden Sartoris
     */
    @Test
    public void verifyEntityIDFilter() {
        val service1 = RegisteredServiceTestUtils.getRegisteredService("urn:abc:def.+");
        service1.setEvaluationOrder(9);

        val service2 = getSamlRegisteredServiceFor(false, false, false, ".+");
        service2.setEvaluationOrder(10);

        servicesManager.save(service1);

        val service = webApplicationServiceFactory.createService("https://sp.testshib.org/shibboleth-sp");

        try (MockedStatic mockedFacade = mockStatic(SamlRegisteredServiceServiceProviderMetadataFacade.class)) {
            //mockedFacade.when(SamlRegisteredServiceServiceProviderMetadataFacade::get)
                //.thenThrow(new IllegalStateException("SamlRegisteredServiceServiceProviderMetadataFacade.get should not be called when requested entityID does not match configured pattern"));
            val res1 = servicesManager.findServiceBy(service);
            assertNull(res1);
            mockedFacade.verify(never(), SamlRegisteredServiceServiceProviderMetadataFacade::get);

            servicesManager.save(service2);

            val res2 = servicesManager.findServiceBy(service);
            assertNotNull(res2);
            mockedFacade.verify(times(1), SamlRegisteredServiceServiceProviderMetadataFacade::get);
        }
    }
}
