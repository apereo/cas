package org.apereo.cas.web.saml2;

import java.nio.file.Files;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.sso.impl.SAML2AuthnRequestBuilder;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import java.io.File;
import java.util.Locale;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTest(
    classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.pac4j.core.session-replication.cookie.crypto.alg=" + ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
        "cas.authn.pac4j.core.session-replication.cookie.crypto.encryption.key=3RXtt06xYUAli7uU-Z915ZGe0MRBFw3uDjWgOEf1GT8",
        "cas.authn.pac4j.core.session-replication.cookie.crypto.signing.key=jIFR-fojN0vOIUcT0hDRXHLVp07CV-YeU8GnjICsXpu65lfkJbiKP028pT74Iurkor38xDGXNcXk_Y1V4rNDqw",
        "cas.authn.pac4j.cookie.enabled=true"
    })
@Tag("Webflow")
class DefaultDelegatedClientAuthenticationWebflowManagerTests {
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

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private JEEContext context;

    private MockRequestContext requestContext;
    
    @BeforeEach
    void setup() throws Exception {
        val service = RegisteredServiceTestUtils.getService();
        requestContext = MockRequestContext.create(applicationContext)
            .withUserAgent("Chrome")
            .setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        context = new JEEContext(requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());
    }
    

    @Test
    void verifyCasStoreOperation() throws Throwable {
        val localeResolver = new SessionLocaleResolver();
        requestContext.setRequestAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, localeResolver);
        val config = new CasConfiguration();
        config.setLoginUrl("https://example.org/login");
        val client = new CasClient();
        client.setConfiguration(config);
        requestContext.setParameter("locale", "de");
        val ticket = delegatedClientAuthenticationWebflowManager.store(requestContext, context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(Locale.GERMAN, localeResolver.resolveLocale(requestContext.getHttpServletRequest()));
    }

    @Test
    void verifySamlStoreOperation() throws Throwable {
        val config = new SAML2Configuration();
        val client = new SAML2Client(config);
        val ticket = delegatedClientAuthenticationWebflowManager.store(requestContext, context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), delegatedClientDistributedSessionStore.get(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).get());
        requestContext.setParameter("RelayState", ticket.getId());
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    void verifyForceAuthnOperation() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        registeredService.setProperties(Map.of(
            RegisteredServiceProperty.RegisteredServiceProperties.DELEGATED_AUTHN_FORCE_AUTHN.getPropertyName(),
            new DefaultRegisteredServiceProperty("true")));
        servicesManager.save(registeredService);

        requestContext.setParameter(CasProtocolConstants.PARAMETER_SERVICE, registeredService.getServiceId());
        val pair = setupTestContextFor(Files.createTempFile("sp-metadata", ".xml").toFile().getAbsolutePath(), "cas.example.sp");
        val ticket = delegatedClientAuthenticationWebflowManager.store(requestContext, context, pair.getLeft());
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), delegatedClientDistributedSessionStore.get(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).get());

        val builder = new SAML2AuthnRequestBuilder();
        val result = builder.build(pair.getRight());
        assertEquals(Boolean.TRUE, result.isForceAuthn());

        requestContext.setParameter("RelayState", ticket.getId());
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, pair.getLeft());
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    void verifyPassiveAuthnOperation() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        registeredService.setProperties(Map.of(
            RegisteredServiceProperty.RegisteredServiceProperties.DELEGATED_AUTHN_PASSIVE_AUTHN.getPropertyName(),
            new DefaultRegisteredServiceProperty("true")));
        servicesManager.save(registeredService);

        requestContext.setParameter(CasProtocolConstants.PARAMETER_SERVICE, registeredService.getServiceId());
        val pair = setupTestContextFor(Files.createTempFile("sp-metadata", ".xml").toFile().getAbsolutePath(), "cas.example.sp");
        val ticket = delegatedClientAuthenticationWebflowManager.store(requestContext, context, pair.getLeft());
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), delegatedClientDistributedSessionStore.get(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).get());

        val builder = new SAML2AuthnRequestBuilder();
        val result = builder.build(pair.getRight());
        assertEquals(Boolean.TRUE, result.isPassive());

        requestContext.setParameter("RelayState", ticket.getId());
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, pair.getLeft());
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    void verifyNoTransientSessionTicketStored() throws Throwable {
        val config = new SAML2Configuration();
        val client = new SAML2Client(config);
        delegatedClientAuthenticationWebflowManager.store(requestContext, context, client);

        requestContext.setParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL);
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client);
        assertEquals(RegisteredServiceTestUtils.CONST_TEST_URL, service.getId());
    }

    @Test
    void verifyExpiredTicketOperation() throws Throwable {
        val config = new SAML2Configuration();
        val client = new SAML2Client(config);
        val ticket = delegatedClientAuthenticationWebflowManager.store(requestContext, context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        assertEquals(ticket.getId(), delegatedClientDistributedSessionStore.get(context,
            SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).get());
        requestContext.setParameter("RelayState", ticket.getId());
        ticket.markTicketExpired();
        assertThrows(UnauthorizedServiceException.class,
            () -> delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client));
    }

    private Pair<SAML2Client, SAML2MessageContext> setupTestContextFor(final String spMetadataPath, final String spEntityId) throws Throwable {
        val idpMetadata = new File("src/test/resources/idp-metadata.xml").getCanonicalPath();
        val keystorePath = new File(FileUtils.getTempDirectory(), "keystore%s".formatted(RandomUtils.randomAlphabetic(4))).getCanonicalPath();
        val saml2ClientConfiguration = new SAML2Configuration(keystorePath, "changeit", "changeit", idpMetadata);
        saml2ClientConfiguration.setServiceProviderEntityId(spEntityId);
        saml2ClientConfiguration.setServiceProviderMetadataPath(spMetadataPath);
        saml2ClientConfiguration.setForceKeystoreGeneration(true);
        saml2ClientConfiguration.setForceServiceProviderMetadataGeneration(true);
        saml2ClientConfiguration.init();

        val saml2Client = new SAML2Client(saml2ClientConfiguration);
        saml2Client.setCallbackUrl("http://callback.example.org");
        saml2Client.init();

        val callContext = new CallContext(this.context, new JEESessionStore());
        val saml2MessageContext = new SAML2MessageContext(callContext);
        saml2MessageContext.setSaml2Configuration(saml2ClientConfiguration);
        val peer = saml2MessageContext.getMessageContext().ensureSubcontext(SAMLPeerEntityContext.class);
        assertNotNull(peer);

        peer.setEntityId("https://cas.example.org/idp");
        val md = peer.ensureSubcontext(SAMLMetadataContext.class);
        assertNotNull(md);
        val roleDescriptorResolver = new PredicateRoleDescriptorResolver(saml2Client.getIdentityProviderMetadataResolver().resolve());
        roleDescriptorResolver.initialize();

        md.setRoleDescriptor(roleDescriptorResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(Objects.requireNonNull(peer.getEntityId())),
            new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME))));

        val self = saml2MessageContext.getMessageContext().ensureSubcontext(SAMLSelfEntityContext.class);
        assertNotNull(self);
        self.setEntityId(saml2ClientConfiguration.getServiceProviderEntityId());

        val sp = self.ensureSubcontext(SAMLMetadataContext.class);
        assertNotNull(sp);
        val spResolver = new PredicateRoleDescriptorResolver(saml2Client.getServiceProviderMetadataResolver().resolve());
        spResolver.initialize();
        sp.setRoleDescriptor(spResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(Objects.requireNonNull(self.getEntityId())),
            new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME))));
        return Pair.of(saml2Client, saml2MessageContext);
    }
}
