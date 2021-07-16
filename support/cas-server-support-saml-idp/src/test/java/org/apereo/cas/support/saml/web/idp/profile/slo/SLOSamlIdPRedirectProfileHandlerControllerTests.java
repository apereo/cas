package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import net.shibboleth.utilities.java.support.net.URLBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
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

import javax.servlet.http.HttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SLOSamlIdPRedirectProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata")
public class SLOSamlIdPRedirectProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {

    @Autowired
    @Qualifier("sloRedirectProfileHandlerController")
    private SLOSamlIdPRedirectProfileHandlerController controller;

    @BeforeEach
    public void initialize() {
        servicesManager.deleteAll();
    }

    @Test
    @Order(1)
    public void verifyOperationRedirectWithParameter() throws Exception {
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
    public void verifyOperationRedirectWithoutParameter() throws Exception {
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
    public void verifyLogoutResponse() throws Exception {
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
        val queryStrings = StringUtils.remove(encoder.getRedirectUrl(), "https://cas.example.org/logout?");
        new URLBuilder(encoder.getRedirectUrl())
            .getQueryParams().forEach(param -> request.addParameter(param.getFirst(), param.getSecond()));
        request.setQueryString(queryStrings);
        controller.handleSaml2ProfileSLORedirectRequest(response, request);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    private void executeTest(final MockHttpServletRequest request, final HttpServletResponse response,
                             final SamlRegisteredService service) throws Exception {
        servicesManager.save(service);
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(LogoutRequest.DEFAULT_ELEMENT_NAME);
        var logoutRequest = (LogoutRequest) builder.buildObject();

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(service.getServiceId());
        logoutRequest.setIssuer(issuer);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        logoutRequest = samlIdPObjectSigner.encode(logoutRequest, service,
            adaptor, response, request, SAMLConstants.SAML2_REDIRECT_BINDING_URI, logoutRequest, new MessageContext());

        val encoder = new SamlIdPHttpRedirectDeflateEncoder("https://cas.example.org/logout", logoutRequest);
        encoder.doEncode();
        val queryStrings = StringUtils.remove(encoder.getRedirectUrl(), "https://cas.example.org/logout?");
        new URLBuilder(encoder.getRedirectUrl())
            .getQueryParams().forEach(param -> request.addParameter(param.getFirst(), param.getSecond()));
        request.setQueryString(queryStrings);
        controller.handleSaml2ProfileSLORedirectRequest(response, request);
    }
}
