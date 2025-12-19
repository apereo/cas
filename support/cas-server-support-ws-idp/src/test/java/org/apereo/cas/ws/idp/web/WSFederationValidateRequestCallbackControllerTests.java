package org.apereo.cas.ws.idp.web;

import module java.base;
import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.SecurityTokenServiceTokenFetcher;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import org.apereo.cas.ws.idp.services.WSFederationRelyingPartyTokenProducer;
import lombok.val;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WSFederationValidateRequestCallbackControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
@TestPropertySource(properties = "cas.tgc.crypto.enabled=false")
@Import(WSFederationValidateRequestCallbackControllerTests.WSFederationValidateRequestCallbackControllerTestConfiguration.class)
class WSFederationValidateRequestCallbackControllerTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier("federationValidateRequestCallbackController")
    private WSFederationValidateRequestCallbackController federationValidateRequestCallbackController;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyWithTicketGrantingTicket() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, registeredService.getRealm());
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);
        request.addParameter(WSFederationConstants.WCTX, UUID.randomUUID().toString());

        var mv = federationValidateRequestCallbackController.handleFederationRequest(response, request);
        assertEquals(CasWebflowConstants.VIEW_ID_ERROR, mv.getViewName());

        val token = new SecurityToken(UUID.randomUUID().toString());

        val id = SecurityTokenTicket.PREFIX + '-' + UUID.randomUUID();
        val sts = mock(SecurityTokenTicket.class);
        when(sts.getPrefix()).thenReturn(SecurityTokenTicket.PREFIX);
        when(sts.getId()).thenReturn(id);
        when(sts.isExpired()).thenReturn(Boolean.FALSE);
        when(sts.getSecurityToken()).thenReturn(token);
        when(sts.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        when(sts.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));

        ticketRegistry.addTicket(sts);

        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.getDescendantTickets().add(id);
        ticketRegistry.addTicket(tgt);

        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        service.getAttributes().put(WSFederationConstants.WREPLY, List.of(registeredService.getServiceId()));

        val st = new MockServiceTicket("123456", service, tgt);
        ticketRegistry.addTicket(st);

        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, st.getId());
        ticketGrantingTicketCookieGenerator.addCookie(request, response, tgt.getId());
        request.setCookies(response.getCookies());

        mv = federationValidateRequestCallbackController.handleFederationRequest(response, request);
        assertEquals(CasWebflowConstants.VIEW_ID_POST_RESPONSE, mv.getViewName());
    }

    @Test
    void verifyWithoutTicketGrantingTicket() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, registeredService.getRealm());
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);
        request.addParameter(WSFederationConstants.WCTX, UUID.randomUUID().toString());

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        service.getAttributes().put(WSFederationConstants.WREPLY, List.of(registeredService.getServiceId()));
        val st = new MockServiceTicket("123456", service, tgt);
        ticketRegistry.addTicket(st);

        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, st.getId());

        ticketGrantingTicketCookieGenerator.addCookie(request, response, tgt.getId());
        request.setCookies(response.getCookies());

        val mv = federationValidateRequestCallbackController.handleFederationRequest(response, request);
        assertEquals(CasWebflowConstants.VIEW_ID_POST_RESPONSE, mv.getViewName());
    }


    private WSFederationRegisteredService getWsFederationRegisteredService() {
        val registeredService = new WSFederationRegisteredService();
        registeredService.setRealm("urn:org:apereo:cas:ws:idp:realm-CAS");
        registeredService.setServiceId("http://app.example5.org/wsfed-idp");
        registeredService.setName("WSFED App");
        registeredService.setId(100);
        registeredService.setAppliesTo("CAS");
        registeredService.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        servicesManager.save(registeredService);
        return registeredService;
    }

    @TestConfiguration(value = "WSFederationValidateRequestCallbackControllerTestConfiguration", proxyBeanMethods = false)
    static class WSFederationValidateRequestCallbackControllerTestConfiguration {
        @Bean
        public WSFederationRelyingPartyTokenProducer wsFederationRelyingPartyTokenProducer() throws Exception {
            val producer = mock(WSFederationRelyingPartyTokenProducer.class);
            when(producer.produce(any(), any(), any(), any(), any())).thenReturn(UUID.randomUUID().toString());
            return producer;
        }

        @Bean
        public SecurityTokenServiceTokenFetcher securityTokenServiceTokenFetcher() throws Throwable {
            val token = new SecurityToken(UUID.randomUUID().toString());
            val fetcher = mock(SecurityTokenServiceTokenFetcher.class);
            when(fetcher.fetch(any(), anyString())).thenReturn(Optional.of(token));
            return fetcher;
        }
    }
}
