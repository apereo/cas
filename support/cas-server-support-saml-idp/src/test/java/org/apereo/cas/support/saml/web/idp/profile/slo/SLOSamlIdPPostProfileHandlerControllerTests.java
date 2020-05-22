package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SLOSamlIdPPostProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.location=file:src/test/resources/metadata")
public class SLOSamlIdPPostProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {

    @Autowired
    @Qualifier("sloPostProfileHandlerController")
    private SLOSamlIdPPostProfileHandlerController controller;

    @Test
    @Order(1)
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceFor(false, false, false, "https://cassp.example.org");
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
            adaptor, response, request, SAMLConstants.SAML2_POST_BINDING_URI, logoutRequest);

        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest).toString();
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, EncodingUtils.encodeBase64(xml));
        controller.handleSaml2ProfileSLOPostRequest(response, request);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
    }
}
