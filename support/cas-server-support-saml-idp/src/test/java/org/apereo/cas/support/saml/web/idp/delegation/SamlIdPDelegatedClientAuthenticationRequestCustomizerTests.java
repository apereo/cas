package org.apereo.cas.support.saml.web.idp.delegation;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.opensaml.saml.saml2.core.IDPList;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Scoping;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPDelegatedClientAuthenticationRequestCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML2")
class SamlIdPDelegatedClientAuthenticationRequestCustomizerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("saml2DelegatedClientAuthenticationRequestCustomizer")
    private DelegatedClientAuthenticationRequestCustomizer customizer;

    @Test
    void verifyScopedIdentityProviderPerServiceImplicitly() throws Throwable {
        val saml2Client = buildMockSaml2Client();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val webContext = new JEEContext(request, response);

        val webApplicationService = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(webApplicationService.getId());
        val delegatedAuthenticationPolicy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        delegatedAuthenticationPolicy.setPermitUndefined(true);
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy().setDelegatedAuthenticationPolicy(delegatedAuthenticationPolicy);
        registeredService.setAccessStrategy(accessStrategy);
        servicesManager.save(registeredService);

        setAuthnRequestFor(webContext, saml2Client.getIdentityProviderResolvedEntityId());

        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService));
    }

    @Test
    void verifyScopedIdentityProviderPerService() throws Throwable {
        val saml2Client = buildMockSaml2Client();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val webContext = new JEEContext(request, response);

        val webApplicationService = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(webApplicationService.getId());
        val delegatedAuthenticationPolicy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        delegatedAuthenticationPolicy.setAllowedProviders(List.of(saml2Client.getName()));
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy().setDelegatedAuthenticationPolicy(delegatedAuthenticationPolicy);
        registeredService.setAccessStrategy(accessStrategy);
        servicesManager.save(registeredService);

        setAuthnRequestFor(webContext, saml2Client.getIdentityProviderResolvedEntityId());
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService));
    }

    @Test
    void verifyAuthorization() throws Throwable {
        val saml2Client = buildMockSaml2Client();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val webContext = new JEEContext(request, response);
        val webApplicationService = CoreAuthenticationTestUtils.getWebApplicationService(UUID.randomUUID().toString());

        assertDoesNotThrow(() -> customizer.customize(saml2Client, webContext));
        
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService));

        setAuthnRequestFor(webContext);
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService));

        setAuthnRequestFor(webContext, UUID.randomUUID().toString());
        assertFalse(customizer.isAuthorized(webContext, saml2Client, webApplicationService));

        
        setAuthnRequestFor(webContext, saml2Client.getIdentityProviderResolvedEntityId());
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService));
        assertDoesNotThrow(() -> customizer.customize(saml2Client, webContext));
        assertTrue(customizer.isAuthorized(webContext, new FormClient(), webApplicationService));
    }

    private void storeRequest(final AuthnRequest authnRequest, final JEEContext webContext) throws Exception {
        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        val context = Pair.of(authnRequest, messageContext);
        ((MockHttpServletRequest) webContext.getNativeRequest()).addParameter(SamlIdPConstants.AUTHN_REQUEST_ID, authnRequest.getID());
        SamlIdPSessionManager.of(openSamlConfigBean, samlIdPDistributedSessionStore).store(webContext, context);
    }

    private void setAuthnRequestFor(final JEEContext webContext,
                                    final String... allowedIdps) throws Exception {
        val service = getSamlRegisteredServiceFor("https://cassp.example.org");
        service.setId(RandomUtils.nextInt());

        val authnRequest = SamlIdPTestUtils.getAuthnRequest(openSamlConfigBean, service);

        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Scoping.DEFAULT_ELEMENT_NAME);
        val scoping = (Scoping) builder.buildObject(Scoping.DEFAULT_ELEMENT_NAME);

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(IDPList.DEFAULT_ELEMENT_NAME);
        val idpList = (IDPList) builder.buildObject(IDPList.DEFAULT_ELEMENT_NAME);

        Arrays.stream(allowedIdps).forEach(idp -> {
            val idpEntry = (IDPEntry) openSamlConfigBean.getBuilderFactory()
                .getBuilder(IDPEntry.DEFAULT_ELEMENT_NAME).buildObject(IDPEntry.DEFAULT_ELEMENT_NAME);
            idpEntry.setProviderID(idp);
            idpList.getIDPEntrys().add(idpEntry);
        });
        scoping.setIDPList(idpList);
        authnRequest.setScoping(scoping);

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        val authnContext = (RequestedAuthnContext) builder.buildObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        authnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        val classRef = (AuthnContextClassRef) builder.buildObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        classRef.setURI(UUID.randomUUID().toString());
        authnContext.getAuthnContextClassRefs().add(classRef);

        authnRequest.setRequestedAuthnContext(authnContext);

        authnRequest.setIsPassive(Boolean.TRUE);
        authnRequest.setForceAuthn(Boolean.TRUE);

        storeRequest(authnRequest, webContext);
    }

    private static SAML2Client buildMockSaml2Client() throws Exception {
        val client = new SAML2Client(getSAML2Configuration());
        client.setCallbackUrl("https://cas.example.org/cas/login");
        client.init(true);
        return client;
    }

    private static SAML2Configuration getSAML2Configuration() throws Exception {
        val idpMetadata = new File("src/test/resources/metadata/idp-metadata.xml").getCanonicalPath();
        val keystorePath = new File(FileUtils.getTempDirectory(), "keystore-" + RandomUtils.nextInt()).getCanonicalPath();
        FileUtils.deleteQuietly(new File(keystorePath));
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata-%s.xml".formatted(RandomUtils.nextInt())).getCanonicalPath();
        FileUtils.deleteQuietly(new File(spMetadataPath));
        val saml2Config = new SAML2Configuration(keystorePath, "changeit", "changeit", idpMetadata);
        saml2Config.setForceKeystoreGeneration(true);

        saml2Config.setForceServiceProviderMetadataGeneration(true);
        saml2Config.setServiceProviderEntityId("cas:example:sp");
        saml2Config.setServiceProviderMetadataPath(spMetadataPath);
        saml2Config.setAuthnRequestBindingType("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        saml2Config.init(true);
        return saml2Config;
    }
}
