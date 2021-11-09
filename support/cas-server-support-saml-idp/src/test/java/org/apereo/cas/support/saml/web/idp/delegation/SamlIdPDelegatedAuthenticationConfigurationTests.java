package org.apereo.cas.support.saml.web.idp.delegation;

import org.apereo.cas.config.SamlIdPDelegatedAuthenticationConfiguration;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlIdPUtils;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.context.SAML2ConfigurationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPDelegatedAuthenticationConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
@Import(SamlIdPDelegatedAuthenticationConfiguration.class)
public class SamlIdPDelegatedAuthenticationConfigurationTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("saml2DelegatedClientAuthenticationRequestCustomizer")
    private DelegatedClientAuthenticationRequestCustomizer customizer;

    @Test
    public void verifyOperation() throws Exception {
        val service = getSamlRegisteredServiceFor("https://cassp.example.org");
        service.setId(2000);
        val authnRequest = SamlIdPTestUtils.getAuthnRequest(openSamlConfigBean, service);
        authnRequest.setForceAuthn(true);
        authnRequest.setIsPassive(true);

        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        val classRef = (AuthnContextClassRef) builder.buildObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        classRef.setURI("https://refeds.org/profile/mfa");

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        val reqCtx = (RequestedAuthnContext) builder.buildObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        reqCtx.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        reqCtx.getAuthnContextClassRefs().add(classRef);

        authnRequest.setRequestedAuthnContext(reqCtx);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val webContext = new JEEContext(request, response);

        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        val context = Pair.of(authnRequest, messageContext);
        SamlIdPUtils.storeSamlRequest(webContext, openSamlConfigBean, samlIdPDistributedSessionStore, context);

        val saml2Client = mock(SAML2Client.class);
        assertTrue(customizer.supports(saml2Client, webContext));
        customizer.customize(saml2Client, webContext);
        assertTrue(webContext.getRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN).isPresent());
        assertTrue(webContext.getRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_PASSIVE).isPresent());
        assertTrue(webContext.getRequestAttribute(SAML2ConfigurationContext.REQUEST_ATTR_AUTHN_CONTEXT_CLASS_REFS).isPresent());
        assertTrue(webContext.getRequestAttribute(SAML2ConfigurationContext.REQUEST_ATTR_COMPARISON_TYPE).isPresent());
    }
}
