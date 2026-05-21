package org.apereo.cas.support.saml.services.logout;

import module java.base;
import org.apereo.cas.logout.slo.ChainingSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.SimpleUrlValidator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("SAMLLogout")
class SamlIdPSingleLogoutServiceLogoutUrlBuilderTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlSingleLogoutServiceLogoutUrlBuilder")
    private SingleLogoutServiceLogoutUrlBuilder samlLogoutUrlBuilder;

    @Test
    void verifyOperation() {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("https://sp.testshib.org/shibboleth-sp");
        val results = samlLogoutUrlBuilder.determineLogoutUrl(samlRegisteredService, service);
        assertFalse(results.isEmpty());
        assertEquals("https://httpbin.org/post", results.iterator().next().getUrl());
        assertTrue(samlLogoutUrlBuilder.supports(samlRegisteredService, service, Optional.of(new MockHttpServletRequest())));
    }

    @Test
    void verifyRedirectOperation() {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("https://mocky.io");
        val results = samlLogoutUrlBuilder.determineLogoutUrl(samlRegisteredService, service);
        assertFalse(results.isEmpty());
        assertEquals("https://httpbin.org/get", results.iterator().next().getUrl());
    }

    @Test
    void verifyChainOperation() {
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
    void verifySoapOperation() {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("urn:soap:slo:example");
        val results = samlLogoutUrlBuilder.determineLogoutUrl(samlRegisteredService, service);
        assertFalse(results.isEmpty());
        assertEquals("https://mocky.io/Shibboleth.sso/SAML2/SOAP", results.iterator().next().getUrl());
    }

    @Test
    void verifyNoOperation() {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("no:slo:service");
        val results = samlLogoutUrlBuilder.determineLogoutUrl(samlRegisteredService, service);
        assertTrue(results.isEmpty());
    }

    @Test
    void verifyFailsLogoutUrl() {
        val results = samlLogoutUrlBuilder.determineLogoutUrl(SamlIdPTestUtils.getSamlRegisteredService(),
            RegisteredServiceTestUtils.getService("https://bad-sp"));
        assertTrue(results.isEmpty());
    }

    @Test
    void verifyBadInput() {
        val results = samlLogoutUrlBuilder.determineLogoutUrl(SamlIdPTestUtils.getSamlRegisteredService(), null);
        assertTrue(results.isEmpty());
    }

    @Test
    void verifySamlLogoutResponse() {
        val samlResponse = "fZFPT4NAEMXv%2FRRk78B23PJnAjRGY9KkXqT24MWsy1qJsEuYxei3l0A1tiad45v3m5mXydafbe"
            + "N96J5qa3K2DDjztFG2qs0hZ4%2B7Oz9h62KRkWwb6HBrD3ZwD5o6a0gvvLFG3hDO%2FZwNvUErqSY0stWETmF5fb9FCDh2"
            + "vXVW2Yadc5cxSaR7N543c5vbnD2%2FJGkCr2KVQAXiKlVpzBO%2BirhUUKVRBLN1%2FxNrHHOEiQa9MeSkcaPMIfK58CHeAS"
            + "AIFHGwTMUTKybzHBonpi%2FenOsIw7C16v0rqG0WnrT%2FEB2WTrqBZu2%2FfmMr7e1lM%2BjLwWlyYzkopYlYeNwRni35FU6fU3wD";
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService("urn:soap:slo:example");
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_SAML_RESPONSE, EncodingUtils.urlDecode(samlResponse));
        val results = samlLogoutUrlBuilder.determineLogoutUrl(samlRegisteredService, service);
        assertFalse(results.isEmpty());
    }
}
