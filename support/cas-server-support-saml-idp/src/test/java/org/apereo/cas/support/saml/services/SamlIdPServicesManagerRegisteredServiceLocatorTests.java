package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @author Hayden Sartoris
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

        val service = webApplicationServiceFactory.createService("https://sp.testshib.org/shibboleth-sp");
        service.setAttributes(Map.of(SamlProtocolConstants.PARAMETER_SAML_REQUEST, List.of("SamlRequest")));

        val result = samlIdPServicesManagerRegisteredServiceLocator.locate(
            (List) candidateServices,
            service, r -> r.matches("https://sp.testshib.org/shibboleth-sp"));
        assertNotNull(result);
    }

    @Test
    public void verifyEntityIdParam() {
        assertNotNull(samlIdPServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE, samlIdPServicesManagerRegisteredServiceLocator.getOrder());
        val service1 = RegisteredServiceTestUtils.getRegisteredService(".+");
        service1.setEvaluationOrder(10);

        val service2 = getSamlRegisteredServiceFor(false, false, false, ".+");
        service2.setEvaluationOrder(9);

        val candidateServices = CollectionUtils.wrapList(service1, service2);
        Collections.sort(candidateServices);

        val service = webApplicationServiceFactory.createService("https://sp.testshib.org/shibboleth-sp");
        service.setAttributes(Map.of(SamlProtocolConstants.PARAMETER_ENTITY_ID, List.of(service.getId())));

        val result = samlIdPServicesManagerRegisteredServiceLocator.locate(
            (List) candidateServices,
            service, r -> r.matches("https://sp.testshib.org/shibboleth-sp"));
        assertNotNull(result);
    }

    @Test
    public void verifyProviderIdParam() {
        assertNotNull(samlIdPServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE, samlIdPServicesManagerRegisteredServiceLocator.getOrder());
        val service1 = RegisteredServiceTestUtils.getRegisteredService(".+");
        service1.setEvaluationOrder(10);

        val service2 = getSamlRegisteredServiceFor(false, false, false, ".+");
        service2.setEvaluationOrder(9);

        val candidateServices = CollectionUtils.wrapList(service1, service2);
        Collections.sort(candidateServices);

        val service = webApplicationServiceFactory.createService("https://sp.testshib.org/shibboleth-sp");
        service.setAttributes(Map.of(SamlIdPConstants.PROVIDER_ID, List.of(service.getId())));

        val result = samlIdPServicesManagerRegisteredServiceLocator.locate(
            (List) candidateServices,
            service, r -> r.matches("https://sp.testshib.org/shibboleth-sp"));
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
        service.setAttributes(Map.of(SamlProtocolConstants.PARAMETER_SAML_REQUEST, List.of("SamlRequest")));
        val result = servicesManager.findServiceBy(service);
        assertNotNull(result);
        assertTrue(result instanceof SamlRegisteredService);
    }

    /**
     * serviceLocator should not trigger metadata lookups when requested
     * entityID does not match pattern for service in question.
     * <p>
     * This test first verifies that, in the case of one service entry that does not match the requested entityID, no
     * metadata lookups are performed. It then verifies that, in the case of two service entries, one matching the
     * requested entityID, exactly one metadata lookup is performed.
     */
    @Test
    public void verifyEntityIDFilter() {
        try (val mockFacade = mockStatic(SamlRegisteredServiceServiceProviderMetadataFacade.class)) {
            val service1 = getSamlRegisteredServiceFor(false, false, false, "urn:abc:def.+");
            service1.setEvaluationOrder(9);
            servicesManager.save(service1);
            mockFacade.when(() -> SamlRegisteredServiceServiceProviderMetadataFacade.get(any(), any(), anyString()))
                .thenCallRealMethod();

            val entityID = "https://sp.testshib.org/shibboleth-sp";
            val service = webApplicationServiceFactory.createService(entityID);
            service.setAttributes(Map.of(SamlProtocolConstants.PARAMETER_SAML_REQUEST, List.of("SamlRequest")));
            val res1 = servicesManager.findServiceBy(service);
            assertNull(res1);

            mockFacade.verify(never(), () -> SamlRegisteredServiceServiceProviderMetadataFacade.get(any(), eq(service1), anyString()));

            val service2 = getSamlRegisteredServiceFor(false, false, false, ".+");
            service2.setEvaluationOrder(10);
            servicesManager.save(service2);

            val res2 = servicesManager.findServiceBy(service);
            assertNotNull(res2);
            mockFacade.verify(() -> SamlRegisteredServiceServiceProviderMetadataFacade.get(any(), eq(service2), eq(entityID)));
        }

    }
}
