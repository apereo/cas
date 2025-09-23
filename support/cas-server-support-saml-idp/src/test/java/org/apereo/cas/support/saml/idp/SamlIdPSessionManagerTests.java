package org.apereo.cas.support.saml.idp;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.pac4j.jee.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPSessionManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("SAML2Web")
@TestPropertySource(properties = "cas.http-client.allow-local-urls=true")
class SamlIdPSessionManagerTests extends BaseSamlIdPConfigurationTests {

    @Test
    void verifySessionOps() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val webContext = new JEEContext(request, response);

        val service = getSamlRegisteredServiceForTestShib();
        for (var i = 1; i < 5; i++) {
            val authnRequest = getAuthnRequestFor(service);
            val messageContext = new MessageContext();
            messageContext.setMessage(authnRequest);
            val payload = Pair.of(authnRequest, messageContext);
            val sessionManager = SamlIdPSessionManager.of(openSamlConfigBean, samlIdPDistributedSessionStore);
            sessionManager.store(webContext, payload);

            request.setParameter(SamlIdPConstants.AUTHN_REQUEST_ID, authnRequest.getID());
            assertDoesNotThrow(() -> sessionManager.fetch(webContext, AuthnRequest.class).orElseThrow());
        }
    }

    @Test
    void verifySessionOpsWithEmbeddedId() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val webContext = new JEEContext(request, response);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        val payload = Pair.of(authnRequest, messageContext);
        val sessionManager = SamlIdPSessionManager.of(openSamlConfigBean, samlIdPDistributedSessionStore);
        sessionManager.store(webContext, payload);
        val serviceUrl = new URIBuilder("https://localhost")
            .appendPath(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_CALLBACK)
            .addParameter(SamlIdPConstants.AUTHN_REQUEST_ID, authnRequest.getID())
            .build()
            .toASCIIString();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceUrl);
        assertDoesNotThrow(() -> sessionManager.fetch(webContext, AuthnRequest.class).orElseThrow());
    }
}
