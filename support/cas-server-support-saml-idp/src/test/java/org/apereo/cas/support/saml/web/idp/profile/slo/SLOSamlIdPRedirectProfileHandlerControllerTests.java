package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import net.shibboleth.shared.net.URLBuilder;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SLOSamlIdPRedirectProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata")
class SLOSamlIdPRedirectProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {

    @Autowired
    @Qualifier("sloRedirectProfileHandlerController")
    private SLOSamlIdPRedirectProfileHandlerController controller;

    @BeforeEach
    void initialize() {
        servicesManager.deleteAll();
    }

    @Test
    @Order(1)
    void verifyOperationRedirectWithParameter() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setMethod("GET");
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceFor(false, false, false, "https://cassp.example.org");
        service.setLogoutUrl("https://github.com/apereo/cas");

        executeTest(request, response, service);

        assertEquals(HttpStatus.SC_OK, response.getStatus());
        assertNotNull(WebUtils.getLogoutRedirectUrl(request, String.class));
    }

    @Test
    @Order(2)
    void verifyOperationRedirectWithoutParameter() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setMethod("GET");
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceFor(false, false, false, "https://cassp.example.org");
        executeTest(request, response, service);

        assertEquals(HttpStatus.SC_OK, response.getStatus());
        assertNull(WebUtils.getLogoutRedirectUrl(request, String.class));
    }

    @Test
    @Order(3)
    void verifyLogoutResponse() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setMethod("GET");
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceFor(false, false, false, "https://cassp.example.org");

        servicesManager.save(service);
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory().getBuilder(LogoutResponse.DEFAULT_ELEMENT_NAME);
        var logoutResponse = (LogoutResponse) builder.buildObject();

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(service.getServiceId());
        logoutResponse.setIssuer(issuer);
        logoutResponse.setID(UUID.randomUUID().toString());
        logoutResponse.setInResponseTo("https://cas.example.org");

        val encoder = new SamlIdPHttpRedirectDeflateEncoder("https://cas.example.org/logout", logoutResponse);
        encoder.setRelayState("CasRelayState");
        encoder.doEncode();

        assertTrue(encoder.getRedirectUrl().contains("CasRelayState"));
        val queryStrings = Strings.CI.remove(encoder.getRedirectUrl(), "https://cas.example.org/logout?");
        new URLBuilder(encoder.getRedirectUrl())
            .getQueryParams().forEach(param -> request.addParameter(param.getFirst(), param.getSecond()));
        request.setQueryString(queryStrings);
        controller.handleSaml2ProfileSLORedirectRequest(response, request);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    private void executeTest(final MockHttpServletRequest request, final HttpServletResponse response,
                             final SamlRegisteredService service) throws Throwable {
        servicesManager.save(service);
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(LogoutRequest.DEFAULT_ELEMENT_NAME);
        var logoutRequest = (LogoutRequest) builder.buildObject();

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(service.getServiceId());
        logoutRequest.setIssuer(issuer);

        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        logoutRequest = samlIdPObjectSigner.encode(logoutRequest, service,
            adaptor, response, request, SAMLConstants.SAML2_REDIRECT_BINDING_URI, logoutRequest, new MessageContext());

        val encoder = new SamlIdPHttpRedirectDeflateEncoder("https://cas.example.org/logout", logoutRequest);
        encoder.doEncode();
        val queryStrings = Strings.CI.remove(encoder.getRedirectUrl(), "https://cas.example.org/logout?");
        new URLBuilder(encoder.getRedirectUrl())
            .getQueryParams().forEach(param -> request.addParameter(param.getFirst(), param.getSecond()));
        request.setQueryString(queryStrings);
        controller.handleSaml2ProfileSLORedirectRequest(response, request);
    }
}
