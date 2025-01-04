package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMonitorAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreValidationAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasSamlIdPAutoConfiguration;
import org.apereo.cas.config.CasThrottlingAutoConfiguration;
import org.apereo.cas.config.CasWebAppAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.RegisteredServicesTemplatesManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataCustomizer;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.util.Saml20HexRandomIdGenerator;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlObjectSignatureValidator;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationName;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.context.SAML2MessageContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseSamlIdPConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SpringBootTest(
    classes = BaseSamlIdPConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "server.port=8383",
        "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS",
        "management.endpoints.web.exposure.include=*",
        "management.endpoints.access.default=UNRESTRICTED",
        
        "cas.webflow.crypto.encryption.key=qLhvLuaobvfzMmbo9U_bYA",
        "cas.webflow.crypto.signing.key=oZeAR5pEXsolruu4OQYsQKxf-FCvFzSsKlsVaKmfIl6pNzoPm6zPW94NRS1af7vT-0bb3DpPBeksvBXjloEsiA",
        "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
        "cas.authn.saml-idp.metadata.http.metadata-backup-location=file://${java.io.tmpdir}/metadata-backups",
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata-${#randomNumber8}"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureObservability
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
public abstract class BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("mockMvc")
    protected MockMvc mockMvc;
    
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    protected CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier(RegisteredServicesTemplatesManager.BEAN_NAME)
    protected RegisteredServicesTemplatesManager registeredServicesTemplatesManager;

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("casSamlIdPMetadataResolver")
    protected MetadataResolver casSamlIdPMetadataResolver;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    protected OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    protected VelocityEngine velocityEngine;

    @Autowired
    @Qualifier(SamlIdPObjectSigner.DEFAULT_BEAN_NAME)
    protected SamlIdPObjectSigner samlIdPObjectSigner;

    @Autowired
    @Qualifier("samlObjectEncrypter")
    protected SamlIdPObjectEncrypter samlIdPObjectEncrypter;

    @Autowired
    @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
    protected SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier(UrlValidator.BEAN_NAME)
    protected UrlValidator urlValidator;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    protected SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlSubjectBuilder")
    protected SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder;

    @Autowired
    @Qualifier("samlProfileSamlConditionsBuilder")
    protected SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder;

    @Autowired
    @Qualifier("samlIdPDistributedSessionStore")
    protected SessionStore samlIdPDistributedSessionStore;

    @Autowired
    @Qualifier("samlObjectSignatureValidator")
    protected SamlObjectSignatureValidator samlObjectSignatureValidator;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(ArgumentExtractor.BEAN_NAME)
    protected ArgumentExtractor argumentExtractor;
    @Autowired
    @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
    protected SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;

    @Autowired
    @Qualifier(SamlIdPMetadataGenerator.BEAN_NAME)
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("samlArtifactMap")
    protected SAMLArtifactMap samlArtifactMap;

    @Autowired
    @Qualifier("samlIdPServicesManagerRegisteredServiceLocator")
    protected ServicesManagerRegisteredServiceLocator samlIdPServicesManagerRegisteredServiceLocator;

    @Autowired
    @Qualifier("defaultAuthnContextClassRefBuilder")
    protected SamlProfileObjectBuilder<AuthnContext> defaultAuthnContextClassRefBuilder;

    protected static AuthenticatedAssertionContext getAssertion() {
        return getAssertion(Map.of());
    }

    protected static AuthenticatedAssertionContext getAssertion(final Map<String, Object> attrs) {
        return getAssertion("casuser", attrs);
    }

    protected static AuthenticatedAssertionContext getAssertion(final String user, final Map<String, Object> attrs) {
        val attributes = new LinkedHashMap<String, Object>(CoreAuthenticationTestUtils.getAttributes());
        attributes.putAll(attrs);

        val permissions = new ArrayList<>();
        permissions.add(new PermissionSamlAttributeValue("admin", "cas-admins", "super-cas"));
        permissions.add(new PermissionSamlAttributeValue("designer", "cas-designers", "cas-ux"));
        attributes.put("permissions", permissions);
        return AuthenticatedAssertionContext.builder()
            .name(user)
            .authenticationDate(ZonedDateTime.now(Clock.systemUTC()))
            .validUntilDate(ZonedDateTime.now(Clock.systemUTC()).plusHours(1))
            .validFromDate(ZonedDateTime.now(Clock.systemUTC()))
            .attributes(attributes)
            .build();
    }

    protected AuthnRequest getAuthnRequestFor(final SamlRegisteredService service) {
        return getAuthnRequestFor(service.getServiceId());
    }

    protected AuthnRequest getAuthnRequestFor(final String service) {
        val authnRequest = samlProfileSamlResponseBuilder.newSamlObject(AuthnRequest.class);
        authnRequest.setID(Saml20HexRandomIdGenerator.INSTANCE.getNewString());
        val issuer = samlProfileSamlResponseBuilder.newSamlObject(Issuer.class);
        issuer.setValue(service);
        authnRequest.setIssuer(issuer);
        authnRequest.setIssueInstant(Instant.now(Clock.systemUTC()));
        return authnRequest;
    }

    protected static SamlRegisteredService getSamlRegisteredServiceForTestShib() {
        return getSamlRegisteredServiceForTestShib(false, false, false);
    }

    protected static SamlRegisteredService getSamlRegisteredServiceForTestShib(final boolean signAssertion,
                                                                               final boolean signResponses) {
        return getSamlRegisteredServiceForTestShib(signAssertion, signResponses, false);
    }

    protected static SamlRegisteredService getSamlRegisteredServiceForTestShib(final boolean signAssertion,
                                                                               final boolean signResponses,
                                                                               final boolean encryptAssertions) {
        return getSamlRegisteredServiceFor(signAssertion, signResponses, encryptAssertions, "https://sp.testshib.org/shibboleth-sp");
    }

    protected static SamlRegisteredService getSamlRegisteredServiceFor(final boolean signAssertion,
                                                                       final boolean signResponses,
                                                                       final boolean encryptAssertions,
                                                                       final String entityId) {
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setServiceId(entityId);
        service.setId(RandomUtils.nextInt());
        service.setSignAssertions(TriStateBoolean.fromBoolean(signAssertion));
        service.setSignResponses(TriStateBoolean.fromBoolean(signResponses));
        service.setEncryptAssertions(encryptAssertions);
        service.setDescription("SAML Service");
        service.setMetadataLocation("classpath:metadata/testshib-providers.xml");
        return service;
    }

    protected static SamlRegisteredService getSamlRegisteredServiceFor(final String entityId) {
        return getSamlRegisteredServiceFor(false, false, false, entityId);
    }

    protected AuthnRequest signAuthnRequest(final HttpServletRequest request, final HttpServletResponse response,
                                            final AuthnRequest authnRequest, final SamlRegisteredService samlRegisteredService) throws Exception {
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            samlRegisteredService, samlRegisteredService.getServiceId()).get();
        return samlIdPObjectSigner.encode(authnRequest, samlRegisteredService,
            adaptor, response, request, SAMLConstants.SAML2_POST_BINDING_URI, authnRequest, new MessageContext());
    }


    protected SAML2MessageContext buildSamlMessageContext() throws Exception {
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
        httpRequest.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; WOW64)");

        val response = new MockHttpServletResponse();
        val ctx = new JEEContext(httpRequest, response);

        var saml2MessageContext = new SAML2MessageContext(new CallContext(ctx, new JEESessionStore()));
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
        return saml2MessageContext;
    }

    @TestConfiguration(value = "SamlIdPMetadataTestConfiguration", proxyBeanMethods = false)
    static class SamlIdPMetadataTestConfiguration {

        @Bean
        public AuthenticationEventExecutionPlanConfigurer samlIdPTestAuthenticationEventExecutionPlanConfigurer(
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final ObjectProvider<PrincipalResolver> defaultPrincipalResolver) {
            return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(
                new SimpleTestUsernamePasswordAuthenticationHandler(), defaultPrincipalResolver.getObject());
        }

        @Bean
        public SamlIdPMetadataCustomizer samlIdPMetadataCustomizer(@Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME) final OpenSamlConfigBean openSamlConfigBean) {
            return (entityDescriptor, registeredService) -> {
                val organization = (Organization) openSamlConfigBean.getBuilderFactory()
                    .getBuilder(Organization.DEFAULT_ELEMENT_NAME).buildObject(Organization.DEFAULT_ELEMENT_NAME);
                val orgName = (OrganizationName) openSamlConfigBean.getBuilderFactory()
                    .getBuilder(OrganizationName.DEFAULT_ELEMENT_NAME).buildObject(OrganizationName.DEFAULT_ELEMENT_NAME);
                orgName.setValue("CASOrganizationName");
                organization.getOrganizationNames().add(orgName);
                entityDescriptor.setOrganization(organization);
            };
        }
    }

    public record PermissionSamlAttributeValue(String type, String group, String user) {
        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("user", user)
                .append("group", group)
                .append("type", type)
                .build();
        }
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreTicketsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreMonitorAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasThrottlingAutoConfiguration.class,
        CasSamlIdPAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreValidationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreSamlAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasWebAppAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(SamlIdPMetadataTestConfiguration.class)
    public static class SharedTestConfiguration {
    }
}
