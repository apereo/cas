package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.DefaultDelegatedClientAuthenticationWebflowManager;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.oauth.client.OAuth10Client;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.config.OAuth10Configuration;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.sso.impl.SAML2AuthnRequestBuilder;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedClientAuthenticationWebflowManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.cookie.enabled=true")
@Tag("Webflow")
public class DefaultDelegatedClientAuthenticationWebflowManagerTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
    private DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager;

    @Autowired
    @Qualifier("delegatedClientDistributedSessionStore")
    private SessionStore delegatedClientDistributedSessionStore;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;
    
    private JEEContext context;

    private MockRequestContext requestContext;

    private MockHttpServletRequest httpServletRequest;

    @BeforeEach
    public void setup() {
        val service = RegisteredServiceTestUtils.getService();
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        context = new JEEContext(httpServletRequest, new MockHttpServletResponse());

        requestContext = new MockRequestContext();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(),
            context.getNativeRequest(), context.getNativeResponse()));
        RequestContextHolder.setRequestContext(requestContext);
        ExternalContextHolder.setExternalContext(requestContext.getExternalContext());
    }

    @Test
    public void verifyOidcStoreOperation() {
        val config = new OidcConfiguration();
        config.setClientId(UUID.randomUUID().toString());
        config.setSecret(UUID.randomUUID().toString());
        val client = new OidcClient(config);
        val ticket = delegatedClientAuthenticationWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertTrue(config.isWithState());
        assertEquals(ticket.getId(), config.getStateGenerator().generateValue(context, JEESessionStore.INSTANCE));

        httpServletRequest.addParameter(OAuth20Configuration.STATE_REQUEST_PARAMETER, ticket.getId());
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifyOAuth2StoreOperation() {
        val config = new OAuth20Configuration();
        config.setKey(UUID.randomUUID().toString());
        config.setSecret(UUID.randomUUID().toString());
        val client = new OAuth20Client();
        client.setConfiguration(config);
        val ticket = delegatedClientAuthenticationWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertTrue(config.isWithState());
        assertEquals(ticket.getId(), config.getStateGenerator().generateValue(context, JEESessionStore.INSTANCE));

        assertThrows(UnauthorizedServiceException.class,
            () -> delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client));

        httpServletRequest.addParameter(OAuth20Configuration.STATE_REQUEST_PARAMETER, ticket.getId());
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client);

        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifyOAuth1StoreOperation() {
        val config = new OAuth10Configuration();
        config.setKey(UUID.randomUUID().toString());
        config.setSecret(UUID.randomUUID().toString());
        val client = new OAuth10Client();
        client.setConfiguration(config);
        val ticket = delegatedClientAuthenticationWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifyCasStoreOperation() {
        val config = new CasConfiguration();
        config.setLoginUrl("https://example.org/login");
        val client = new CasClient();
        client.setConfiguration(config);
        val ticket = delegatedClientAuthenticationWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), config.getCustomParams().get(DefaultDelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID));
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifySamlStoreOperation() {
        val config = new SAML2Configuration();
        val client = new SAML2Client(config);
        val ticket = delegatedClientAuthenticationWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), delegatedClientDistributedSessionStore.get(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).get());

        httpServletRequest.addParameter("RelayState", ticket.getId());
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifyForceAuthnOperation() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        registeredService.setProperties(Map.of(
            RegisteredServiceProperty.RegisteredServiceProperties.DELEGATED_AUTHN_FORCE_AUTHN.getPropertyName(),
            new DefaultRegisteredServiceProperty("true")));
        servicesManager.save(registeredService);
        
        httpServletRequest.setParameter(CasProtocolConstants.PARAMETER_SERVICE, registeredService.getServiceId());
        val pair = setupTestContextFor(File.createTempFile("sp-metadata", ".xml").getAbsolutePath(), "cas.example.sp");
        val ticket = delegatedClientAuthenticationWebflowManager.store(context, pair.getLeft());
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), delegatedClientDistributedSessionStore.get(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).get());

        val builder = new SAML2AuthnRequestBuilder();
        val result = builder.build(pair.getRight());
        assertTrue(result.isForceAuthn());

        httpServletRequest.addParameter("RelayState", ticket.getId());
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, pair.getLeft());
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifyPassiveAuthnOperation() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        registeredService.setProperties(Map.of(
            RegisteredServiceProperty.RegisteredServiceProperties.DELEGATED_AUTHN_PASSIVE_AUTHN.getPropertyName(),
            new DefaultRegisteredServiceProperty("true")));
        servicesManager.save(registeredService);

        httpServletRequest.setParameter(CasProtocolConstants.PARAMETER_SERVICE, registeredService.getServiceId());
        val pair = setupTestContextFor(File.createTempFile("sp-metadata", ".xml").getAbsolutePath(), "cas.example.sp");
        val ticket = delegatedClientAuthenticationWebflowManager.store(context, pair.getLeft());
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), delegatedClientDistributedSessionStore.get(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).get());

        val builder = new SAML2AuthnRequestBuilder();
        val result = builder.build(pair.getRight());
        assertTrue(result.isPassive());

        httpServletRequest.addParameter("RelayState", ticket.getId());
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, pair.getLeft());
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void verifyExpiredTicketOperation() {
        val config = new SAML2Configuration();
        val client = new SAML2Client(config);
        val ticket = delegatedClientAuthenticationWebflowManager.store(context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), delegatedClientDistributedSessionStore.get(context,
            SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).get());
        httpServletRequest.addParameter("RelayState", ticket.getId());
        ticket.markTicketExpired();
        assertThrows(UnauthorizedServiceException.class,
            () -> delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client));
    }

    private Pair<SAML2Client, SAML2MessageContext> setupTestContextFor(final String spMetadataPath, final String spEntityId) throws Exception {
        val idpMetadata = new File("src/test/resources/idp-metadata.xml").getCanonicalPath();
        val keystorePath = new File(FileUtils.getTempDirectory(), "keystore").getCanonicalPath();
        val saml2ClientConfiguration = new SAML2Configuration(keystorePath, "changeit", "changeit", idpMetadata);
        saml2ClientConfiguration.setServiceProviderEntityId(spEntityId);
        saml2ClientConfiguration.setServiceProviderMetadataPath(spMetadataPath);
        saml2ClientConfiguration.setForceKeystoreGeneration(true);
        saml2ClientConfiguration.setForceServiceProviderMetadataGeneration(true);
        saml2ClientConfiguration.init();

        val saml2Client = new SAML2Client(saml2ClientConfiguration);
        saml2Client.setCallbackUrl("http://callback.example.org");
        saml2Client.init();

        val saml2MessageContext = new SAML2MessageContext();
        saml2MessageContext.setSaml2Configuration(saml2ClientConfiguration);
        saml2MessageContext.setWebContext(context);
        val peer = saml2MessageContext.getMessageContext().getSubcontext(SAMLPeerEntityContext.class, true);
        assertNotNull(peer);

        peer.setEntityId("https://cas.example.org/idp");
        val md = peer.getSubcontext(SAMLMetadataContext.class, true);
        assertNotNull(md);
        val roleDescriptorResolver = new PredicateRoleDescriptorResolver(saml2Client.getIdpMetadataResolver().resolve());
        roleDescriptorResolver.initialize();

        md.setRoleDescriptor(roleDescriptorResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(Objects.requireNonNull(peer.getEntityId())),
            new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME))));

        val self = saml2MessageContext.getMessageContext().getSubcontext(SAMLSelfEntityContext.class, true);
        assertNotNull(self);
        self.setEntityId(saml2ClientConfiguration.getServiceProviderEntityId());

        val sp = self.getSubcontext(SAMLMetadataContext.class, true);
        assertNotNull(sp);
        val spResolver = new PredicateRoleDescriptorResolver(saml2Client.getSpMetadataResolver().resolve());
        spResolver.initialize();
        sp.setRoleDescriptor(spResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(Objects.requireNonNull(self.getEntityId())),
            new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME))));
        return Pair.of(saml2Client, saml2MessageContext);
    }
}
