package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.idp.MissingSamlAuthnRequestException;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.util.Saml20HexRandomIdGenerator;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;
import static org.apereo.cas.util.junit.Assertions.assertThrowsWithRootCause;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link SSOSamlIdPProfileCallbackHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
class SSOSamlIdPProfileCallbackHandlerControllerTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata")
    class MvcTests extends BaseSamlIdPConfigurationTests {

        @Test
        void verifyOperation() throws Exception {
            val mv = mockMvc.perform(get(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_CALLBACK)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            ).andExpect(status().isBadRequest()).andReturn().getModelAndView();
            assertEquals(SamlIdPConstants.VIEW_ID_SAML_IDP_ERROR, mv.getViewName());
            assertTrue(mv.getModel().containsKey(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION));
            val error = mv.getModel().get(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION);
            assertInstanceOf(MissingSamlAuthnRequestException.class, error);
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata")
    class DefaultTests extends BaseSamlIdPConfigurationTests {
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
        void verifyNoRequest() {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            assertThrowsWithRootCause(RuntimeException.class, MissingSamlAuthnRequestException.class,
                () -> controller.handleCallbackProfileRequestGet(response, request));
        }

        @Test
        void verifyNoTicketPassiveAuthn() throws Throwable {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val authnRequest = signAuthnRequest(request, response, getAuthnRequest(true));

            val context = new MessageContext();
            context.setMessage(authnRequest);

            storeAuthnRequest(request, response, authnRequest, context);

            val mv = controller.handleCallbackProfileRequestGet(response, request);
            assertNull(mv);
            assertEquals(HttpStatus.OK.value(), response.getStatus());

            val samlResponse = (Response) request.getAttribute(Response.class.getName());
            assertEquals(StatusCode.NO_PASSIVE, samlResponse.getStatus().getStatusCode().getValue());
        }

        @Test
        void verifyNoTicket() throws Throwable {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val authnRequest = signAuthnRequest(request, response, getAuthnRequest());

            val context = new MessageContext();
            context.setMessage(authnRequest);

            storeAuthnRequest(request, response, authnRequest, context);

            val mv = controller.handleCallbackProfileRequestGet(response, request);
            assertEquals(HttpStatus.BAD_REQUEST, mv.getStatus());
        }

        @Test
        void verifyValidationByPost() throws Throwable {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            val authn = getAuthnRequest();
            authn.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
            val authnRequest = signAuthnRequest(request, response, authn);
            val context = new MessageContext();
            context.setMessage(authnRequest);

            storeAuthnRequest(request, response, authnRequest, context);

            val st1 = getServiceTicket();
            request.addParameter(CasProtocolConstants.PARAMETER_TICKET, st1.getId());
            controller.handleCallbackProfileRequestGet(response, request);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
        }

        @Test
        void verifyValidationByRedirect() throws Throwable {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            val authn = getAuthnRequest();
            authn.setProtocolBinding(SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI);
            val authnRequest = signAuthnRequest(request, response, authn);

            val context = new MessageContext();
            context.setMessage(authnRequest);

            storeAuthnRequest(request, response, authnRequest, context);

            val st1 = getServiceTicket();
            request.addParameter(CasProtocolConstants.PARAMETER_TICKET, st1.getId());
            controller.handleCallbackProfileRequestGet(response, request);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
        }

        private AuthnRequest signAuthnRequest(final HttpServletRequest request,
                                              final HttpServletResponse response,
                                              final AuthnRequest authnRequest) throws Throwable {
            val adaptor = SamlRegisteredServiceMetadataAdaptor
                .get(samlRegisteredServiceCachingMetadataResolver, samlRegisteredService,
                    samlRegisteredService.getServiceId()).get();
            return samlIdPObjectSigner.encode(authnRequest, samlRegisteredService,
                adaptor, response, request, SAMLConstants.SAML2_POST_BINDING_URI, authnRequest, new MessageContext());
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

        private AuthnRequest getAuthnRequest() {
            return getAuthnRequest(false);
        }

        private AuthnRequest getAuthnRequest(final boolean passive) {
            var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
                .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
            var authnRequest = (AuthnRequest) builder.buildObject();
            authnRequest.setID(Saml20HexRandomIdGenerator.INSTANCE.getNewString());
            builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
                .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
            val issuer = (Issuer) builder.buildObject();
            issuer.setValue(samlRegisteredService.getServiceId());
            authnRequest.setIssuer(issuer);
            authnRequest.setIsPassive(passive);
            return authnRequest;
        }

        private void storeAuthnRequest(final MockHttpServletRequest request, final MockHttpServletResponse response,
                                       final AuthnRequest authnRequest, final MessageContext context) throws Throwable {
            request.addParameter(SamlIdPConstants.AUTHN_REQUEST_ID, authnRequest.getID());
            SamlIdPSessionManager.of(openSamlConfigBean, samlIdPDistributedSessionStore)
                .store(new JEEContext(request, response), Pair.of(authnRequest, context));
        }
    }
}
