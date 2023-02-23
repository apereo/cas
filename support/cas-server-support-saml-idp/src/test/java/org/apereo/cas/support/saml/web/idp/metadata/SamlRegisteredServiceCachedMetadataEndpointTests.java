package org.apereo.cas.support.saml.web.idp.metadata;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.UUID;

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
public class SamlRegisteredServiceCachedMetadataEndpointTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlRegisteredServiceCachedMetadataEndpoint")
    private SamlRegisteredServiceCachedMetadataEndpoint endpoint;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    public void beforeEach() {
        this.samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        servicesManager.save(samlRegisteredService);
    }

    @Test
    public void verifyInvalidate() {
        endpoint.invalidate(samlRegisteredService.getServiceId(), null);
        endpoint.invalidate(StringUtils.EMPTY, null);
    }

    @Test
    public void verifyInvalidateByEntityId() {
        endpoint.getCachedMetadataObject(samlRegisteredService.getServiceId(), samlRegisteredService.getServiceId(), true);
        endpoint.invalidate(samlRegisteredService.getName(), samlRegisteredService.getServiceId());
        val response = endpoint.getCachedMetadataObject(samlRegisteredService.getServiceId(), samlRegisteredService.getServiceId(), false);
        assertNotNull(response);
        assertFalse(response.getBody().containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    public void verifyCachedMetadataObject() {
        val response = endpoint.getCachedMetadataObject(samlRegisteredService.getServiceId(), StringUtils.EMPTY, true);
        val results = response.getBody();
        assertNotNull(results);
        assertTrue(results.containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    public void verifyCachedServiceWithoutResolution() {
        val response = endpoint.getCachedMetadataObject(String.valueOf(samlRegisteredService.getName()), UUID.randomUUID().toString(), false);
        val results = response.getBody();
        assertNotNull(results);
        assertFalse(results.containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    public void verifyCachedService() {
        Arrays.asList(Boolean.TRUE, Boolean.FALSE).forEach(force -> {
            val response = endpoint.getCachedMetadataObject(String.valueOf(samlRegisteredService.getId()), StringUtils.EMPTY, force);
            val results = response.getBody();
            assertNotNull(results);
            assertTrue(results.containsKey(samlRegisteredService.getServiceId()));
        });
    }

    @Test
    public void verifyBadService() {
        val response = endpoint.getCachedMetadataObject("bad-service-id", StringUtils.EMPTY, true);
        val results = response.getBody();
        assertNotNull(results);
        assertTrue(results.containsKey("error"));
    }
}
