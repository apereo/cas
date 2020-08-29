package org.apereo.cas.support.saml.services.logout;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceLogoutUrlBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("SAML")
public class SamlIdPSingleLogoutServiceLogoutUrlBuilderTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifyOperation() {
        val builder = new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager,
            defaultSamlRegisteredServiceCachingMetadataResolver);
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("https://sp.testshib.org/shibboleth-sp");
        val results = builder.determineLogoutUrl(samlRegisteredService, service);
        assertFalse(results.isEmpty());
        assertEquals("https://httpbin.org/post", results.iterator().next().getUrl());
        assertTrue(builder.supports(samlRegisteredService, service, Optional.of(new MockHttpServletRequest())));
    }

    @Test
    public void verifyRedirectOperation() {
        val builder = new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager,
            defaultSamlRegisteredServiceCachingMetadataResolver);
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("https://mocky.io");
        val results = builder.determineLogoutUrl(samlRegisteredService, service);
        assertFalse(results.isEmpty());
        assertEquals("https://httpbin.org/get", results.iterator().next().getUrl());
    }

    @Test
    public void verifyFailsLogoutUrl() {
        val builder = new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager,
            defaultSamlRegisteredServiceCachingMetadataResolver);
        val results = builder.determineLogoutUrl(SamlIdPTestUtils.getSamlRegisteredService(),
            RegisteredServiceTestUtils.getService("https://bad-sp"));
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyBadInput() {
        val builder = new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager,
            defaultSamlRegisteredServiceCachingMetadataResolver);
        val results = builder.determineLogoutUrl(SamlIdPTestUtils.getSamlRegisteredService(), null);
        assertTrue(results.isEmpty());
    }
}
