package org.apereo.cas.support.saml.services.logout;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceLogoutUrlBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
            defaultSamlRegisteredServiceCachingMetadataResolver,
            urlValidator);
        val results = builder.determineLogoutUrl(SamlIdPTestUtils.getSamlRegisteredService(),
            RegisteredServiceTestUtils.getService("https://sp.testshib.org/shibboleth-sp"));
        assertFalse(results.isEmpty());
        assertEquals("https://httpbin.org/post", results.iterator().next().getUrl());
    }
}
