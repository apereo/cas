package org.apereo.cas.support.saml.services.logout;

import org.apereo.cas.logout.slo.ChainingSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.web.SimpleUrlValidator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
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
    @Autowired
    @Qualifier("samlSingleLogoutServiceLogoutUrlBuilder")
    private SingleLogoutServiceLogoutUrlBuilder samlLogoutUrlBuilder;

    @Test
    public void verifyOperation() {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("https://sp.testshib.org/shibboleth-sp");
        val results = samlLogoutUrlBuilder.determineLogoutUrl(samlRegisteredService, service);
        assertFalse(results.isEmpty());
        assertEquals("https://httpbin.org/post", results.iterator().next().getUrl());
        assertTrue(samlLogoutUrlBuilder.supports(samlRegisteredService, service, Optional.of(new MockHttpServletRequest())));
    }

    @Test
    public void verifyRedirectOperation() {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("https://mocky.io");
        val results = samlLogoutUrlBuilder.determineLogoutUrl(samlRegisteredService, service);
        assertFalse(results.isEmpty());
        assertEquals("https://httpbin.org/get", results.iterator().next().getUrl());
    }

    @Test
    public void verifyChainOperation() {
        val defaultBuilder = new DefaultSingleLogoutServiceLogoutUrlBuilder(servicesManager, SimpleUrlValidator.getInstance());
        val chain = new ChainingSingleLogoutServiceLogoutUrlBuilder(List.of(samlLogoutUrlBuilder, defaultBuilder));

        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        samlRegisteredService.setLogoutType(RegisteredServiceLogoutType.FRONT_CHANNEL);
        val service = RegisteredServiceTestUtils.getService("https://mocky.io");
        val results = chain.determineLogoutUrl(samlRegisteredService, service);
        assertEquals(1, results.size());
        val res = results.iterator().next();
        assertTrue(res.getProperties().containsKey(
            SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING));
    }

    @Test
    public void verifySoapOperation() {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("urn:soap:slo:example");
        val results = samlLogoutUrlBuilder.determineLogoutUrl(samlRegisteredService, service);
        assertFalse(results.isEmpty());
        assertEquals("https://mocky.io/Shibboleth.sso/SAML2/SOAP", results.iterator().next().getUrl());
    }

    @Test
    public void verifyNoOperation() {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("no:slo:service");
        val results = samlLogoutUrlBuilder.determineLogoutUrl(samlRegisteredService, service);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyFailsLogoutUrl() {
        val results = samlLogoutUrlBuilder.determineLogoutUrl(SamlIdPTestUtils.getSamlRegisteredService(),
            RegisteredServiceTestUtils.getService("https://bad-sp"));
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyBadInput() {
        val results = samlLogoutUrlBuilder.determineLogoutUrl(SamlIdPTestUtils.getSamlRegisteredService(), null);
        assertTrue(results.isEmpty());
    }
}
