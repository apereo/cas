package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.authentication.SamlIdPAuthenticationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
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
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.pac4j.core.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.sso.impl.SAML2AuthnRequestBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.apereo.cas.authentication.CoreAuthenticationTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuthnRequestRequestedAttributesAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.session-replication.cookie.auto-configure-cookie-path=true",
    "cas.authn.saml-idp.core.session-storage-type=TICKET_REGISTRY",
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata4"
})
public class AuthnRequestRequestedAttributesAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "AuthnRequestRequestedAttributesAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private SAML2Configuration saml2Configuration;

    private SAML2MessageContext saml2MessageContext;

    @BeforeEach
    public void initialize() throws Exception {
        val idpMetadata = new File("src/test/resources/metadata/idp-metadata.xml").getCanonicalPath();
        val keystorePath = new File(FileUtils.getTempDirectory(), "keystore").getCanonicalPath();
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata.xml").getCanonicalPath();

        saml2Configuration = new SAML2Configuration(keystorePath,
            "changeit", "changeit", idpMetadata);
        saml2Configuration.setServiceProviderEntityId("cas:example:sp");
        saml2Configuration.setServiceProviderMetadataPath(spMetadataPath);
        saml2Configuration.init();

        val saml2Client = new SAML2Client(saml2Configuration);
        saml2Client.setCallbackUrl("http://callback.example.org");
        saml2Client.init();

        saml2MessageContext = new SAML2MessageContext();
        saml2MessageContext.setSaml2Configuration(saml2Configuration);
        saml2MessageContext.setWebContext(new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse()));
        val peer = saml2MessageContext.getMessageContext().getSubcontext(SAMLPeerEntityContext.class, true);
        assertNotNull(peer);
        peer.setEntityId("https://cas.example.org/idp");
        val md = peer.getSubcontext(SAMLMetadataContext.class, true);
        assertNotNull(md);
        val idpResolver = SamlIdPUtils.getRoleDescriptorResolver(casSamlIdPMetadataResolver, true);
        md.setRoleDescriptor(idpResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(Objects.requireNonNull(peer.getEntityId())),
            new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME))));
        val self = saml2MessageContext.getMessageContext().getSubcontext(SAMLSelfEntityContext.class, true);
        assertNotNull(self);
        self.setEntityId(saml2Configuration.getServiceProviderEntityId());

        val sp = self.getSubcontext(SAMLMetadataContext.class, true);
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
    public void verifySerializationToJson() throws IOException {
        val filter = new AuthnRequestRequestedAttributesAttributeReleasePolicy();
        filter.setUseFriendlyName(true);
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, AuthnRequestRequestedAttributesAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
        assertNotNull(strategyRead.toString());
    }

    @Test
    public void verifyNoAuthnRequest() {
        val filter = new AuthnRequestRequestedAttributesAttributeReleasePolicy();
        filter.setAllowedAttributes(List.of("eduPersonPrincipalAttribute"));
        filter.setUseFriendlyName(true);

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        assertThrows(IllegalArgumentException.class,
            () -> filter.getAttributes(getPrincipal("casuser",
                    CollectionUtils.wrap("eduPersonPrincipalName", "casuser")),
                CoreAuthenticationTestUtils.getService(), registeredService));
    }

    @Test
    public void verifyAuthnRequestWithoutExtensions() throws IOException {
        val filter = new AuthnRequestRequestedAttributesAttributeReleasePolicy();
        filter.setAllowedAttributes(List.of("eduPersonPrincipalAttribute"));
        filter.setUseFriendlyName(true);

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);

        val builder = new SAML2AuthnRequestBuilder();
        val authnRequest = builder.build(saml2MessageContext);

        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest)) {
            val samlRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));

            val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
            val context = new JEEContext(request, response);

            samlIdPDistributedSessionStore.set(context, SamlProtocolConstants.PARAMETER_SAML_REQUEST, samlRequest);
            val messageContext = new MessageContext();
            messageContext.setMessage(authnRequest);
            samlIdPDistributedSessionStore.set(context, MessageContext.class.getName(),
                SamlIdPAuthenticationContext.from(messageContext).encode());

            val attributes = filter.getAttributes(getPrincipal("casuser",
                    CollectionUtils.wrap("eduPersonPrincipalName", "casuser")),
                CoreAuthenticationTestUtils.getService(), registeredService);
            assertTrue(attributes.isEmpty());
        }
    }

    @Test
    public void verifyAuthnRequestWithExtensionsNotAllowed() throws IOException {
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

        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest)) {
            val samlRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));

            val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
            val context = new JEEContext(request, response);

            samlIdPDistributedSessionStore.set(context, SamlProtocolConstants.PARAMETER_SAML_REQUEST, samlRequest);
            val messageContext = new MessageContext();
            messageContext.setMessage(authnRequest);
            samlIdPDistributedSessionStore.set(context, MessageContext.class.getName(),
                SamlIdPAuthenticationContext.from(messageContext).encode());

            val attributes = filter.getAttributes(getPrincipal("casuser",
                    CollectionUtils.wrap("eduPersonPrincipalName", "casuser", "givenName", "CAS")),
                CoreAuthenticationTestUtils.getService(), registeredService);
            assertTrue(attributes.isEmpty());
        }
    }

    @Test
    public void verifyAuthnRequestWithExtensionsAllowed() throws IOException {
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

        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest)) {
            val samlRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));

            val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
            val context = new JEEContext(request, response);

            samlIdPDistributedSessionStore.set(context, SamlProtocolConstants.PARAMETER_SAML_REQUEST, samlRequest);
            val messageContext = new MessageContext();
            messageContext.setMessage(authnRequest);
            samlIdPDistributedSessionStore.set(context, MessageContext.class.getName(),
                SamlIdPAuthenticationContext.from(messageContext).encode());

            val attributes = filter.getAttributes(getPrincipal("casuser",
                    CollectionUtils.wrap("eduPersonPrincipalName", "casuser", "givenName", "CAS")),
                CoreAuthenticationTestUtils.getService(), registeredService);
            assertTrue(attributes.containsKey("eduPersonPrincipalName"));

            val definitions = filter.determineRequestedAttributeDefinitions(getPrincipal("casuser"),
                registeredService, CoreAuthenticationTestUtils.getService());
            assertTrue(definitions.contains("eduPersonPrincipalName"));
        }
    }
}
