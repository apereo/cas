package org.apereo.cas.support.saml.web.idp.delegation;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.SamlIdPDelegatedAuthenticationConfiguration;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlIdPUtils;

import lombok.val;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.opensaml.saml.saml2.core.IDPList;
import org.opensaml.saml.saml2.core.Scoping;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPDelegatedClientAuthenticationRequestCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
@Import(SamlIdPDelegatedAuthenticationConfiguration.class)
public class SamlIdPDelegatedClientAuthenticationRequestCustomizerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("saml2DelegatedClientAuthenticationRequestCustomizer")
    private DelegatedClientAuthenticationRequestCustomizer customizer;

    @Test
    public void verifyAuthorization() throws Exception {
        val saml2Client = mock(SAML2Client.class);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val webContext = new JEEContext(request, response);
        val webApplicationService = CoreAuthenticationTestUtils.getWebApplicationService();

        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService));

        setAuthnRequestFor(webContext);
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService));

        setAuthnRequestFor(webContext, UUID.randomUUID().toString());
        assertFalse(customizer.isAuthorized(webContext, saml2Client, webApplicationService));

        val providerId = UUID.randomUUID().toString();
        when(saml2Client.getIdentityProviderResolvedEntityId()).thenReturn(providerId);
        setAuthnRequestFor(webContext, providerId);
        assertTrue(customizer.isAuthorized(webContext, saml2Client, webApplicationService));

        assertTrue(customizer.isAuthorized(webContext, new CasClient(), webApplicationService));
    }

    private void storeRequest(final AuthnRequest authnRequest, final JEEContext webContext) throws Exception {
        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        val context = Pair.of(authnRequest, messageContext);
        SamlIdPUtils.storeSamlRequest(webContext, openSamlConfigBean, samlIdPDistributedSessionStore, context);
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
        storeRequest(authnRequest, webContext);
    }
}
