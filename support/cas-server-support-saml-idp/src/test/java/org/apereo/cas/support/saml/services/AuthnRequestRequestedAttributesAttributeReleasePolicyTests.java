package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.pac4j.core.context.CallContext;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.sso.impl.SAML2AuthnRequestBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuthnRequestRequestedAttributesAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAMLAttributes")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.session-replication.cookie.auto-configure-cookie-path=true",
    "cas.authn.saml-idp.core.session-storage-type=TICKET_REGISTRY",
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata46"
})
class AuthnRequestRequestedAttributesAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "AuthnRequestRequestedAttributesAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private SAML2MessageContext saml2MessageContext;

    @BeforeEach
    public void initialize() throws Throwable {
        val idpMetadata = new File("src/test/resources/metadata/idp-metadata.xml").getCanonicalPath();
        val keystorePath = new File(FileUtils.getTempDirectory(), "keystore").getCanonicalPath();
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata.xml").getCanonicalPath();

        val saml2Configuration = new SAML2Configuration(keystorePath,
            "changeit", "changeit", idpMetadata);
        saml2Configuration.setServiceProviderEntityId("cas:example:sp");
        saml2Configuration.setServiceProviderMetadataPath(spMetadataPath);
        saml2Configuration.init();

        val saml2Client = new SAML2Client(saml2Configuration);
        saml2Client.setCallbackUrl("http://callback.example.org");
        saml2Client.init();

        val httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");

        val response = new MockHttpServletResponse();
        val ctx = new JEEContext(httpRequest, response);

        saml2MessageContext = new SAML2MessageContext(new CallContext(ctx, new JEESessionStore()));
        saml2MessageContext.setSaml2Configuration(saml2Configuration);

        val peer = saml2MessageContext.getMessageContext().ensureSubcontext(SAMLPeerEntityContext.class);
        assertNotNull(peer);
        peer.setEntityId("https://cas.example.org/idp");
        val md = peer.ensureSubcontext(SAMLMetadataContext.class);
        assertNotNull(md);
        val idpResolver = SamlIdPUtils.getRoleDescriptorResolver(casSamlIdPMetadataResolver, true);
        md.setRoleDescriptor(idpResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(Objects.requireNonNull(peer.getEntityId())),
            new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME))));
        val self = saml2MessageContext.getMessageContext().ensureSubcontext(SAMLSelfEntityContext.class);
        assertNotNull(self);
        self.setEntityId(saml2Configuration.getServiceProviderEntityId());
        val sp = self.ensureSubcontext(SAMLMetadataContext.class);
        assertNotNull(sp);
        val spRes = new InMemoryResourceMetadataResolver(new File(spMetadataPath), openSamlConfigBean);
        spRes.setId(getClass().getSimpleName());
        spRes.initialize();
        val spResolver = SamlIdPUtils.getRoleDescriptorResolver(spRes, true);
        sp.setRoleDescriptor(spResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(Objects.requireNonNull(self.getEntityId())),
            new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME))));
    }

    @Test
    void verifySerializationToJson() throws IOException {
        val filter = new AuthnRequestRequestedAttributesAttributeReleasePolicy();
        filter.setUseFriendlyName(true);
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, AuthnRequestRequestedAttributesAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
        assertNotNull(strategyRead.toString());
    }

    @Test
    void verifyNoAuthnRequest() throws Throwable {
        val filter = new AuthnRequestRequestedAttributesAttributeReleasePolicy();
        filter.setAllowedAttributes(List.of("eduPersonPrincipalAttribute"));
        filter.setUseFriendlyName(true);

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);

        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("eduPersonPrincipalName", "casuser")))
            .build();
        assertThrows(IllegalArgumentException.class, () -> filter.getAttributes(context));
    }

    @Test
    void verifyAuthnRequestWithoutExtensions() throws Throwable {
        val filter = new AuthnRequestRequestedAttributesAttributeReleasePolicy();
        filter.setAllowedAttributes(List.of("eduPersonPrincipalAttribute"));
        filter.setUseFriendlyName(true);

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);

        val builder = new SAML2AuthnRequestBuilder();
        val authnRequest = builder.build(saml2MessageContext);
        storeSamlAuthnRequest(authnRequest);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("eduPersonPrincipalName", "casuser")))
            .build();
        val attributes = filter.getAttributes(releasePolicyContext);
        assertTrue(attributes.isEmpty());
    }

    @Test
    void verifyAuthnRequestWithExtensionsNotAllowed() throws Throwable {
        val filter = new AuthnRequestRequestedAttributesAttributeReleasePolicy();
        filter.setAllowedAttributes(List.of("eduPersonPrincipalAttribute"));
        filter.setUseFriendlyName(false);

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);

        val builder = new SAML2AuthnRequestBuilder();
        val authnRequest = builder.build(saml2MessageContext);
        val extensions = ((SAMLObjectBuilder<Extensions>) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Extensions.DEFAULT_ELEMENT_NAME)).buildObject();

        val attrBuilder = (SAMLObjectBuilder<RequestedAttribute>)
            openSamlConfigBean.getBuilderFactory().getBuilder(RequestedAttribute.DEFAULT_ELEMENT_NAME);

        val requestAttribute = attrBuilder.buildObject(RequestedAttribute.DEFAULT_ELEMENT_NAME);
        requestAttribute.setIsRequired(false);
        requestAttribute.setName("givenName");
        extensions.getUnknownXMLObjects().add(requestAttribute);
        authnRequest.setExtensions(extensions);

        storeSamlAuthnRequest(authnRequest);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("eduPersonPrincipalName", "casuser", "givenName", "CAS")))
            .build();
        val attributes = filter.getAttributes(releasePolicyContext);
        assertTrue(attributes.isEmpty());
    }

    @Test
    void verifyAuthnRequestWithExtensionsAllowed() throws Throwable {
        val filter = new AuthnRequestRequestedAttributesAttributeReleasePolicy();
        filter.setAllowedAttributes(List.of("eduPersonPrincipalName"));
        filter.setUseFriendlyName(false);

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);

        val builder = new SAML2AuthnRequestBuilder();
        val authnRequest = builder.build(saml2MessageContext);
        val extensions = ((SAMLObjectBuilder<Extensions>) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Extensions.DEFAULT_ELEMENT_NAME)).buildObject();

        val attrBuilder = (SAMLObjectBuilder<RequestedAttribute>)
            openSamlConfigBean.getBuilderFactory().getBuilder(RequestedAttribute.DEFAULT_ELEMENT_NAME);

        val requestAttribute = attrBuilder.buildObject(RequestedAttribute.DEFAULT_ELEMENT_NAME);
        requestAttribute.setIsRequired(false);
        requestAttribute.setName("eduPersonPrincipalName");
        extensions.getUnknownXMLObjects().add(requestAttribute);
        authnRequest.setExtensions(extensions);
        authnRequest.setID(UUID.randomUUID().toString());

        storeSamlAuthnRequest(authnRequest);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("eduPersonPrincipalName", "casuser", "givenName", "CAS")))
            .build();
        val attributes = filter.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("eduPersonPrincipalName"));
        val definitions = filter.determineRequestedAttributeDefinitions(releasePolicyContext);
        assertTrue(definitions.contains("eduPersonPrincipalName"));
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
