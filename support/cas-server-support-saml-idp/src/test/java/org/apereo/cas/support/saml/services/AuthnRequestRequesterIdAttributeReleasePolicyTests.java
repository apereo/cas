package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequesterID;
import org.opensaml.saml.saml2.core.Scoping;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.sso.impl.SAML2AuthnRequestBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuthnRequestRequesterIdAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("SAMLAttributes")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.session-replication.cookie.crypto.enabled=true",
    "cas.authn.saml-idp.core.session-replication.cookie.auto-configure-cookie-path=true",
    "cas.authn.saml-idp.core.session-storage-type=TICKET_REGISTRY",
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata6146"
})
class AuthnRequestRequesterIdAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "AuthnRequestRequesterIdAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private SAML2MessageContext saml2MessageContext;

    @BeforeEach
    void initialize() throws Throwable {
        this.saml2MessageContext = buildSamlMessageContext();
    }

    
    @Test
    void verifySerializationToJson() {
        val filter = new AuthnRequestRequesterIdAttributeReleasePolicy();
        filter.setRequesterIdPattern("sp-entity-id");
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, AuthnRequestRequesterIdAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
        assertNotNull(strategyRead.toString());
    }

    @Test
    void verifyAuthnRequestWithRequesterIDs() throws Throwable {
        val filter = new AuthnRequestRequesterIdAttributeReleasePolicy();
        filter.setAllowedAttributes(List.of("eduPersonPrincipalName"));
        filter.setRequesterIdPattern(".+sp.testshib.org.*");

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);

        val builder = new SAML2AuthnRequestBuilder();
        val authnRequest = builder.build(saml2MessageContext);

        val scoping = ((SAMLObjectBuilder<Scoping>) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Scoping.DEFAULT_ELEMENT_NAME)).buildObject();
        val requesterId = ((SAMLObjectBuilder<RequesterID>) openSamlConfigBean.getBuilderFactory()
            .getBuilder(RequesterID.DEFAULT_ELEMENT_NAME)).buildObject();
        requesterId.setURI("https://sp.testshib.org/shibboleth-sp");
        scoping.getRequesterIDs().add(requesterId);

        authnRequest.setScoping(scoping);
        storeSamlAuthnRequest(authnRequest);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext
            .builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("eduPersonPrincipalName", "casuser", "givenName", "CAS")))
            .build();
        val attributes = filter.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("eduPersonPrincipalName"));
    }

    private void storeSamlAuthnRequest(@NotNull final AuthnRequest authnRequest) throws Throwable {
        val request = (MockHttpServletRequest) HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        request.setParameter(SamlIdPConstants.AUTHN_REQUEST_ID, authnRequest.getID());

        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
        val context = new JEEContext(request, response);
        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        SamlIdPSessionManager.of(openSamlConfigBean, samlIdPDistributedSessionStore)
            .store(context, Pair.of(authnRequest, messageContext));
    }
}
