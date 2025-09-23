package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.util.Saml20HexRandomIdGenerator;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SSOSamlIdPProfileCallbackHandlerControllerWithBrowserStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.session-storage-type=BROWSER_STORAGE",
    "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata"
})
class SSOSamlIdPProfileCallbackHandlerControllerWithBrowserStorageTests extends BaseSamlIdPConfigurationTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(false).build().toObjectMapper();
    
    @Autowired
    @Qualifier("ssoPostProfileCallbackHandlerController")
    private SSOSamlIdPProfileCallbackHandlerController controller;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
    }

    @Test
    void verifyReadFromStorage() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val authn = getAuthnRequest();
        authn.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        storeAuthnRequest(request, response, authn);

        val st = getServiceTicket();
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, st.getId());
        val mv = controller.handleCallbackProfileRequestGet(response, request);
        assertEquals(CasWebflowConstants.VIEW_ID_BROWSER_STORAGE_READ, mv.getViewName());
        assertTrue(mv.getModel().containsKey(BrowserStorage.PARAMETER_BROWSER_STORAGE));
    }

    @Test
    void verifyResumeFromStorage() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val authn = getAuthnRequest();
        authn.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        storeAuthnRequest(request, response, authn);

        val st = getServiceTicket();
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, st.getId());
        val storage = samlIdPDistributedSessionStore.getTrackableSession(new JEEContext(request, response))
            .map(BrowserStorage.class::cast)
            .orElseThrow();
        request.addParameter(BrowserStorage.PARAMETER_BROWSER_STORAGE,
            MAPPER.writeValueAsString(Map.of(storage.getContext(), storage.getPayload())));
        val mv = controller.handleCallbackProfileRequestPost(response, request);
        assertNull(mv);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    private void storeAuthnRequest(final MockHttpServletRequest request, final MockHttpServletResponse response,
                                   final AuthnRequest authnRequest) throws Throwable {
        val context = new MessageContext();
        context.setMessage(authnRequest);
        request.addParameter(SamlIdPConstants.AUTHN_REQUEST_ID, authnRequest.getID());
        SamlIdPSessionManager.of(openSamlConfigBean, samlIdPDistributedSessionStore)
            .store(new JEEContext(request, response), Pair.of(authnRequest, context));
    }
    
    private AuthnRequest getAuthnRequest() {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        var authnRequest = (AuthnRequest) builder.buildObject();
        authnRequest.setID(Saml20HexRandomIdGenerator.INSTANCE.getNewString());
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(samlRegisteredService.getServiceId());
        authnRequest.setIssuer(issuer);
        return authnRequest;
    }

    private ServiceTicket getServiceTicket() throws Throwable {
        val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        ticketRegistry.addTicket(tgt);
        val trackingPolicy = mock(TicketTrackingPolicy.class);
        val ticketService = RegisteredServiceTestUtils.getService(samlRegisteredService.getServiceId());
        ticketService.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, List.of(samlRegisteredService.getServiceId()));
        val st1 = tgt.grantServiceTicket(ticketService, trackingPolicy);
        ticketRegistry.addTicket(st1);
        ticketRegistry.updateTicket(tgt);
        return st1;
    }
}
