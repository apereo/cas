package org.apereo.cas.ws.idp.web;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import lombok.val;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WSFederationValidateRequestControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = "cas.tgc.crypto.enabled=false")
@Tag("WSFederation")
class WSFederationValidateRequestControllerTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier("federationValidateRequestController")
    private WSFederationValidateRequestController federationValidateRequestController;

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
    void verifyNoWa() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertThrows(UnauthorizedAuthenticationException.class,
            () -> federationValidateRequestController.handleFederationRequest(response, request));
    }

    @Test
    void verifyLogoutWithReply() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, registeredService.getRealm());
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNOUT10);
        request.addParameter(WSFederationConstants.WHR, "whr");
        request.addParameter(WSFederationConstants.WREQ, "wreq");

        assertDoesNotThrow(() -> {
            federationValidateRequestController.handleFederationRequest(response, request);
            return null;
        });
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        assertEquals("https://cas.example.org:8443/cas/logout?service=http://app.example5.org/wsfed-idp", response.getHeader("Location"));
    }

    @Test
    void verifyUnauthzServicesWithUnknownRealm() {
        val request = new MockHttpServletRequest();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, "unknown");
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNOUT10);
        val wsFedRequest = WSFederationRequest.of(request);
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        assertThrows(UnauthorizedServiceException.class,
            () -> federationValidateRequestController.findAndValidateFederationRequestForRegisteredService(service, wsFedRequest));
    }

    @Test
    void verifyUnauthzServicesWithMismatchedRealm() {
        val request = new MockHttpServletRequest();

        val registeredService = getWsFederationRegisteredService("custom-realm");
        request.addParameter(WSFederationConstants.WTREALM, "custom-realm");
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNOUT10);
        val wsFedRequest = WSFederationRequest.of(request);
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        assertThrows(UnauthorizedServiceException.class,
            () -> federationValidateRequestController.findAndValidateFederationRequestForRegisteredService(service, wsFedRequest));
    }


    @Test
    void verifyLogoutWithoutReply() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNOUT10);

        assertDoesNotThrow(() -> {
            federationValidateRequestController.handleFederationRequest(response, request);
            return null;
        });
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        assertEquals("https://cas.example.org:8443/cas/logout", response.getHeader("Location"));
    }

    @Test
    void verifyLogin() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, registeredService.getRealm());
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);

        assertDoesNotThrow(() -> federationValidateRequestController.handleFederationRequest(response, request));
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());

        val builder = new URIBuilder(response.getHeader("Location"));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_SERVICE)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WTREALM)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WREPLY)));
    }

    @Test
    void verifyLoginRenewWithNoToken() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, registeredService.getRealm());
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WREFRESH, "5000");
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        assertDoesNotThrow(() -> federationValidateRequestController.handleFederationRequest(response, request));
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        val builder = new URIBuilder(response.getHeader("Location"));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_SERVICE)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_RENEW)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WTREALM)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WREPLY)));
    }

    @Test
    void verifyLoginRenewDisabled() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, registeredService.getRealm());
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WREFRESH, "0");
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        assertDoesNotThrow(() -> federationValidateRequestController.handleFederationRequest(response, request));
        assertNotNull(federationValidateRequestController.handleUnauthorizedServiceException(request, new RuntimeException()));
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        val builder = new URIBuilder(response.getHeader("Location"));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_SERVICE)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WTREALM)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WREPLY)));
    }

    @Test
    void verifyLoginRenewWithToken() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, registeredService.getRealm());
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WREFRESH, "1");
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);

        val token = mock(SecurityToken.class);
        when(token.isExpired()).thenReturn(Boolean.FALSE);
        when(token.getCreated()).thenReturn(Instant.now(Clock.systemUTC()).minusSeconds(300));

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
        ticketGrantingTicketCookieGenerator.addCookie(request, response, tgt.getId());
        request.setCookies(response.getCookies());

        assertDoesNotThrow(() -> federationValidateRequestController.handleFederationRequest(response, request));
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        val builder = new URIBuilder(response.getHeader("Location"));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_SERVICE)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_RENEW)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WTREALM)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WREPLY)));
    }

    private WSFederationRegisteredService getWsFederationRegisteredService() {
        return getWsFederationRegisteredService("urn:org:apereo:cas:ws:idp:realm-CAS");
    }

    private WSFederationRegisteredService getWsFederationRegisteredService(final String realm) {
        val registeredService = new WSFederationRegisteredService();
        registeredService.setRealm(realm);
        registeredService.setServiceId("http://app.example5.org/wsfed-idp");
        registeredService.setName("WSFED App");
        registeredService.setId(100);
        registeredService.setAppliesTo("CAS");
        registeredService.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        servicesManager.save(registeredService);
        return registeredService;
    }

}
