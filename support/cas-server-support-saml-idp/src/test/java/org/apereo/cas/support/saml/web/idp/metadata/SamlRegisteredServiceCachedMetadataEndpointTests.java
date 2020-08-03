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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRegisteredServiceCachedMetadataEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
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
        endpoint.invalidate(samlRegisteredService.getServiceId());
        endpoint.invalidate(StringUtils.EMPTY);
    }

    @Test
    public void verifyCachedMetadataObject() {
        val results = endpoint.getCachedMetadataObject(samlRegisteredService.getServiceId(), StringUtils.EMPTY);
        assertTrue(results.containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    public void verifyCachedService() {
        val results = endpoint.getCachedMetadataObject(String.valueOf(samlRegisteredService.getId()), StringUtils.EMPTY);
        assertTrue(results.containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    public void verifyBadService() {
        val results = endpoint.getCachedMetadataObject("bad-service-id", StringUtils.EMPTY);
        assertTrue(results.containsKey("error"));
    }
}
