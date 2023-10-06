package org.apereo.cas.support.saml.web.idp.metadata;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRegisteredServiceCachedMetadataEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLMetadata")
@TestPropertySource(properties = {
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.samlIdPRegisteredServiceMetadataCache.enabled=true"
})
@Execution(ExecutionMode.SAME_THREAD)
class SamlRegisteredServiceCachedMetadataEndpointTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlRegisteredServiceCachedMetadataEndpoint")
    private SamlRegisteredServiceCachedMetadataEndpoint endpoint;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    public void beforeEach() {
        samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        servicesManager.save(samlRegisteredService);
    }

    @Test
    void verifyInvalidate() throws Throwable {
        endpoint.invalidate(samlRegisteredService.getServiceId(), null);
        endpoint.invalidate(StringUtils.EMPTY, null);
    }

    @Test
    void verifyInvalidateByEntityId() throws Throwable {
        endpoint.getCachedMetadataObject(samlRegisteredService.getServiceId(), samlRegisteredService.getServiceId(), true);
        endpoint.invalidate(samlRegisteredService.getName(), samlRegisteredService.getServiceId());
        val response = endpoint.getCachedMetadataObject(samlRegisteredService.getServiceId(), samlRegisteredService.getServiceId(), false);
        assertNotNull(response);
        assertFalse(response.getBody().containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    void verifyCachedMetadataObject() throws Throwable {
        val response = endpoint.getCachedMetadataObject(samlRegisteredService.getServiceId(), StringUtils.EMPTY, true);
        val results = response.getBody();
        assertNotNull(results);
        assertTrue(results.containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    void verifyCachedServiceWithoutResolution() throws Throwable {
        val response = endpoint.getCachedMetadataObject(String.valueOf(samlRegisteredService.getName()), UUID.randomUUID().toString(), false);
        val results = response.getBody();
        assertNotNull(results);
        assertFalse(results.containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    void verifyCachedService() throws Throwable {
        Stream.of(Boolean.TRUE, Boolean.FALSE)
            .map(force -> endpoint.getCachedMetadataObject(String.valueOf(samlRegisteredService.getId()), StringUtils.EMPTY, force))
            .map(HttpEntity::getBody)
            .forEach(results -> {
                assertNotNull(results);
                assertTrue(results.containsKey(samlRegisteredService.getServiceId()));
            });
    }

    @Test
    void verifyBadService() throws Throwable {
        val response = endpoint.getCachedMetadataObject("bad-service-id", StringUtils.EMPTY, true);
        val results = response.getBody();
        assertNotNull(results);
        assertTrue(results.containsKey("error"));
    }
}
