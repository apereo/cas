package org.apereo.cas.support.saml.web.idp.profile.sso;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.util.Saml20HexRandomIdGenerator;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link SSOSamlIdPPostProfileHandlerControllerWithTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata",
    "cas.authn.saml-idp.core.session-storage-type=TICKET_REGISTRY",

    "cas.authn.saml-idp.core.session-replication.cookie.crypto.enabled=true",
    "cas.authn.saml-idp.core.session-replication.cookie.crypto.alg=" + ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
    "cas.authn.saml-idp.core.session-replication.cookie.crypto.encryption.key=3RXtt06xYUAli7uU-Z915ZGe0MRBFw3uDjWgOEf1GT8",
    "cas.authn.saml-idp.core.session-replication.cookie.crypto.signing.key=jIFR-fojN0vOIUcT0hDRXHLVp07CV-YeU8GnjICsXpu65lfkJbiKP028pT74Iurkor38xDGXNcXk_Y1V4rNDqw",

    "cas.authn.saml-idp.core.session-replication.cookie.crypto.enabled=false"
})
class SSOSamlIdPPostProfileHandlerControllerWithTicketRegistryTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlIdPDistributedSessionCookieGenerator")
    private CasCookieBuilder samlIdPDistributedSessionCookieGenerator;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
    }

    @Test
    void verifyPostSignRequest() throws Throwable {
        samlIdPDistributedSessionCookieGenerator.setCookiePath(StringUtils.EMPTY);
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, getAuthnRequest()).toString();
        val result = performPostSignRequest("/custompath", EncodingUtils.encodeBase64(xml));
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertEquals("/custompath/", samlIdPDistributedSessionCookieGenerator.getCookiePath());
        samlIdPDistributedSessionCookieGenerator.setCookiePath("/custom");
        val customResult = performPostSignRequest("/custompath", EncodingUtils.encodeBase64(xml));
        assertEquals(HttpStatus.FOUND.value(), customResult.getResponse().getStatus());
        assertEquals("/custom", samlIdPDistributedSessionCookieGenerator.getCookiePath());
    }

    private MvcResult performPostSignRequest(final String contextPath, final String samlRequest) throws Exception {
        return mockMvc.perform(post(contextPath + SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST)
            .contextPath(contextPath)
            .header(HttpHeaders.USER_AGENT, "Firefox")
            .param(SamlProtocolConstants.PARAMETER_SAML_REQUEST, samlRequest))
            .andReturn();
    }

    private AuthnRequest getAuthnRequest() {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        val authnRequest = (AuthnRequest) Objects.requireNonNull(builder).buildObject();
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) Objects.requireNonNull(builder).buildObject();
        issuer.setValue(samlRegisteredService.getServiceId());
        authnRequest.setIssuer(issuer);
        authnRequest.setID(Saml20HexRandomIdGenerator.INSTANCE.getNewString());
        return authnRequest;
    }
}
