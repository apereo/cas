package org.apereo.cas.support.saml.web.idp.delegation;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasSamlIdPAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.CasSessionStore;
import org.apereo.cas.web.saml2.BaseSaml2DelegatedAuthenticationTests;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.opensaml.saml.saml2.core.IDPList;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Scoping;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.properties.KeystoreProperties;
import org.pac4j.core.config.properties.ResourceProperties;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.context.SAML2ConfigurationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPDelegatedClientAuthenticationRequestCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = {
    CasSamlIdPAutoConfiguration.class,
    BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class
}, properties = "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml4223")
@Tag("SAML2Web")
@ExtendWith(CasTestExtension.class)
class SamlIdPDelegatedClientAuthenticationRequestCustomizerTests {
    private static final String SERVICE_PROVIDER_ENTITY_ID = "https://cassp.example.org";

    @Autowired
    @Qualifier("saml2DelegatedClientAuthenticationRequestCustomizer")
    private DelegatedClientAuthenticationRequestCustomizer customizer;

    @Autowired
    @Qualifier(SamlIdPDelegatedClientAuthenticationRequestCustomizer.BEAN_NAME_SESSION_STORE)
    private CasSessionStore samlIdPDistributedSessionStore;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyCustomization() throws Throwable {
        val authnRequest = buildAuthnRequest();
        authnRequest.setForceAuthn(true);
        authnRequest.setIsPassive(true);
        authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext("https://refeds.org/profile/mfa"));

        val requestContext = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());
        storeRequest(authnRequest, webContext);

        val saml2Client = mock(SAML2Client.class);
        assertTrue(customizer.supports(saml2Client, webContext, requestContext));
        customizer.customize(saml2Client, webContext, requestContext);
        assertTrue(webContext.getRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN).isPresent());
        assertTrue(webContext.getRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_PASSIVE).isPresent());
        assertTrue(webContext.getRequestAttribute(SAML2ConfigurationContext.REQUEST_ATTR_AUTHN_CONTEXT_CLASS_REFS).isPresent());
        assertTrue(webContext.getRequestAttribute(SAML2ConfigurationContext.REQUEST_ATTR_COMPARISON_TYPE).isPresent());
    }

    @Test
    void verifyScopedIdentityProviderPerServiceImplicitly() throws Throwable {
        val saml2Client = buildMockSaml2Client();
        val requestContext = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());

        val webApplicationService = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(webApplicationService.getId());
        val delegatedAuthenticationPolicy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        delegatedAuthenticationPolicy.setPermitUndefined(true);
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setDelegatedAuthenticationPolicy(delegatedAuthenticationPolicy));
        servicesManager.save(registeredService);

        setAuthnRequestFor(webContext, saml2Client.getIdentityProviderResolvedEntityId());
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService, requestContext));
    }

    @Test
    void verifyScopedIdentityProviderPerService() throws Throwable {
        val saml2Client = buildMockSaml2Client();
        val requestContext = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());

        val webApplicationService = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(webApplicationService.getId());
        val delegatedAuthenticationPolicy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        delegatedAuthenticationPolicy.setAllowedProviders(List.of(saml2Client.getName()));
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setDelegatedAuthenticationPolicy(delegatedAuthenticationPolicy));
        servicesManager.save(registeredService);

        setAuthnRequestFor(webContext, saml2Client.getIdentityProviderResolvedEntityId());
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService, requestContext));
    }

    /**
     * An authentication request that scopes authentication to some other identity provider must not
     * authorize this client. That denial then falls through to the delegated authentication policy of
     * whichever service definition the services manager resolves, and every registered service carries
     * a default policy that permits undefined providers, so the outcome of the fallback is decided
     * entirely by what the registry happens to contain. Asserting it against the shared registry would
     * mean depending on no sibling test in this Spring context having saved a catch-all service
     * definition, which this test cannot hold on its own; both fallback outcomes are therefore asserted
     * against an isolated services manager. The assertions that do not reach the fallback continue to
     * use the wired customizer.
     */
    @Test
    void verifyAuthorization() throws Throwable {
        val saml2Client = buildMockSaml2Client();
        val requestContext = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());
        val webApplicationService = CoreAuthenticationTestUtils.getWebApplicationService(UUID.randomUUID().toString());

        assertDoesNotThrow(() -> customizer.customize(saml2Client, webContext, requestContext));
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService, requestContext));

        setAuthnRequestFor(webContext);
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService, requestContext));

        setAuthnRequestFor(webContext, UUID.randomUUID().toString());
        assertFalse(customizerResolving(null)
            .isAuthorized(webContext, saml2Client, webApplicationService, requestContext));
        val permittingService = RegisteredServiceTestUtils.getRegisteredService(webApplicationService.getId());
        assertTrue(customizerResolving(permittingService)
            .isAuthorized(webContext, saml2Client, webApplicationService, requestContext));

        setAuthnRequestFor(webContext, saml2Client.getIdentityProviderResolvedEntityId());
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService, requestContext));
        assertDoesNotThrow(() -> customizer.customize(saml2Client, webContext, requestContext));
        assertTrue(customizer.isAuthorized(webContext, mock(IndirectClient.class), webApplicationService, requestContext));
    }

    /**
     * Builds a customizer whose service lookups resolve to the given service definition, or to nothing
     * when it is null, so that the delegated authentication policy fallback can be asserted without
     * depending on the contents of the registry shared by this Spring context.
     *
     * @param registeredService the service definition to resolve, or null to resolve none
     * @return the customizer
     */
    private DelegatedClientAuthenticationRequestCustomizer customizerResolving(final RegisteredService registeredService) {
        val isolatedServicesManager = mock(ServicesManager.class);
        when(isolatedServicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        return new SamlIdPDelegatedClientAuthenticationRequestCustomizer(openSamlConfigBean, isolatedServicesManager, casProperties);
    }

    private void storeRequest(final AuthnRequest authnRequest, final JEEContext webContext) throws Exception {
        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        val context = Pair.of(authnRequest, messageContext);
        val request = (MockHttpServletRequest) webContext.getNativeRequest();
        request.setParameter(SamlIdPConstants.AUTHN_REQUEST_ID, authnRequest.getID());
        SamlIdPSessionManager.of(openSamlConfigBean, samlIdPDistributedSessionStore)
            .store(request, webContext.getNativeResponse(), context);
    }

    private void setAuthnRequestFor(final JEEContext webContext, final String... allowedIdps) throws Exception {
        val authnRequest = buildAuthnRequest();

        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory().getBuilder(Scoping.DEFAULT_ELEMENT_NAME);
        val scoping = (Scoping) Objects.requireNonNull(builder).buildObject(Scoping.DEFAULT_ELEMENT_NAME);
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory().getBuilder(IDPList.DEFAULT_ELEMENT_NAME);
        val idpList = (IDPList) Objects.requireNonNull(builder).buildObject(IDPList.DEFAULT_ELEMENT_NAME);
        Arrays.stream(allowedIdps).forEach(idp -> {
            val idpEntry = (IDPEntry) openSamlConfigBean.getBuilderFactory()
                .getBuilder(IDPEntry.DEFAULT_ELEMENT_NAME).buildObject(IDPEntry.DEFAULT_ELEMENT_NAME);
            idpEntry.setProviderID(idp);
            idpList.getIDPEntrys().add(idpEntry);
        });
        scoping.setIDPList(idpList);
        authnRequest.setScoping(scoping);
        authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext(UUID.randomUUID().toString()));
        authnRequest.setIsPassive(Boolean.TRUE);
        authnRequest.setForceAuthn(Boolean.TRUE);
        storeRequest(authnRequest, webContext);
    }

    private AuthnRequest buildAuthnRequest() {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory().getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        val authnRequest = (AuthnRequest) Objects.requireNonNull(builder).buildObject();
        authnRequest.setID("_" + RandomUtils.randomAlphanumeric(16));
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) Objects.requireNonNull(builder).buildObject();
        issuer.setValue(SERVICE_PROVIDER_ENTITY_ID);
        authnRequest.setIssuer(issuer);
        return authnRequest;
    }

    private RequestedAuthnContext buildRequestedAuthnContext(final String authnContextClass) {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory().getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        val classRef = (AuthnContextClassRef) Objects.requireNonNull(builder).buildObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        classRef.setURI(authnContextClass);
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory().getBuilder(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        val requestedAuthnContext = (RequestedAuthnContext) Objects.requireNonNull(builder).buildObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        requestedAuthnContext.getAuthnContextClassRefs().add(classRef);
        return requestedAuthnContext;
    }

    private static SAML2Client buildMockSaml2Client() throws Exception {
        val client = new SAML2Client(getSAML2Configuration());
        client.setCallbackUrl("https://cas.example.org/cas/login");
        client.init(true);
        return client;
    }

    private static SAML2Configuration getSAML2Configuration() throws Exception {
        val idpMetadata = new File("src/test/resources/idp-metadata.xml").getCanonicalPath();
        val keystorePath = new File(FileUtils.getTempDirectory(), "keystore-" + RandomUtils.nextInt()).getCanonicalPath();
        FileUtils.deleteQuietly(new File(keystorePath));
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata-%s.xml".formatted(RandomUtils.nextInt())).getCanonicalPath();
        FileUtils.deleteQuietly(new File(spMetadataPath));
        val saml2Config = new SAML2Configuration(new KeystoreProperties(keystorePath)
            .setKeystorePassword("changeit")
            .setPrivateKeyPassword("changeit"),
            new ResourceProperties(idpMetadata));
        saml2Config.getKeystore().setForceKeystoreGeneration(true);
        saml2Config.setForceServiceProviderMetadataGeneration(true);
        saml2Config.setServiceProviderEntityId("cas:example:sp");
        saml2Config.getServiceProviderMetadata().setPath(spMetadataPath);
        saml2Config.setAuthnRequestBindingType("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        saml2Config.init(true);
        return saml2Config;
    }
}
