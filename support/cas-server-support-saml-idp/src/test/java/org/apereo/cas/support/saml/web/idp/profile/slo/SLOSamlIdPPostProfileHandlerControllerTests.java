package org.apereo.cas.support.saml.web.idp.profile.slo;

import module java.base;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.junit.Assertions;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link SLOSamlIdPPostProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
class SLOSamlIdPPostProfileHandlerControllerTests {

    @TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata")
    abstract static class BaseTests extends BaseSamlIdPConfigurationTests {
        protected MvcResult performSloPostRequest(final String samlRequest) throws Exception {
            return mockMvc.perform(post(SamlIdPConstants.ENDPOINT_SAML2_SLO_PROFILE_POST)
                .param(SamlProtocolConstants.PARAMETER_SAML_REQUEST, samlRequest)).andReturn();
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.saml-idp.logout.force-signed-logout-requests=true")
    class MissingSignatureTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            val service = getSamlRegisteredServiceFor(false, false, false, "https://cassp.example.org");
            servicesManager.save(service);
            var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
                .getBuilder(LogoutRequest.DEFAULT_ELEMENT_NAME);
            var logoutRequest = (LogoutRequest) Objects.requireNonNull(builder).buildObject();

            builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
                .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
            val issuer = (Issuer) Objects.requireNonNull(builder).buildObject();
            issuer.setValue(service.getServiceId());
            logoutRequest.setIssuer(issuer);

            val adaptor = SamlRegisteredServiceMetadataAdaptor
                .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).orElseThrow();
            logoutRequest = samlIdPObjectSigner.encode(logoutRequest, service,
                adaptor, response, request, SAMLConstants.SAML2_POST_BINDING_URI, logoutRequest, new MessageContext());

            val xml = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest).toString();
            val result = performSloPostRequest(EncodingUtils.encodeBase64(xml));
            assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
        }

        @Test
        void verifyUnsignedRequestFails() {
            val service = getSamlRegisteredServiceFor(false, false, false, "https://cassp.example.org");
            servicesManager.save(service);
            var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
                .getBuilder(LogoutRequest.DEFAULT_ELEMENT_NAME);
            var logoutRequest = (LogoutRequest) Objects.requireNonNull(builder).buildObject();

            builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
                .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
            val issuer = (Issuer) Objects.requireNonNull(builder).buildObject();
            issuer.setValue(service.getServiceId());
            logoutRequest.setIssuer(issuer);

            val xml = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest).toString();
            Assertions.assertThrowsWithRootCause(Exception.class, SAMLException.class,
                () -> performSloPostRequest(EncodingUtils.encodeBase64(xml)));
        }
    }
    
    @Nested
    @TestPropertySource(properties = "cas.authn.saml-idp.logout.force-signed-logout-requests=false")
    class AllowedMissingSignatureTests extends BaseTests {
        @Test
        void verifyUnsignedRequestAllowed() throws Throwable {
            val service = getSamlRegisteredServiceFor(false, false, false, "https://cassp.example.org");
            service.setSignLogoutRequest(TriStateBoolean.FALSE);
            servicesManager.save(service);
            var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
                .getBuilder(LogoutRequest.DEFAULT_ELEMENT_NAME);
            var logoutRequest = (LogoutRequest) Objects.requireNonNull(builder).buildObject();

            builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
                .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
            val issuer = (Issuer) Objects.requireNonNull(builder).buildObject();
            issuer.setValue(service.getServiceId());
            logoutRequest.setIssuer(issuer);

            val xml = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest).toString();
            val result = performSloPostRequest(EncodingUtils.encodeBase64(xml));
            assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
            assertNotNull(WebUtils.getSingleLogoutRequest(result.getRequest()));
        }
    }
}
