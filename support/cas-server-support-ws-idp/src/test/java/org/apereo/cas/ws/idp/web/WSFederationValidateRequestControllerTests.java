package org.apereo.cas.ws.idp.web;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.val;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;
import java.time.Instant;
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
public class WSFederationValidateRequestControllerTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier("federationValidateRequestController")
    private WSFederationValidateRequestController federationValidateRequestController;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Test
    public void verifyNoWa() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertThrows(UnauthorizedAuthenticationException.class,
            () -> federationValidateRequestController.handleFederationRequest(response, request));
    }

    @Test
    public void verifyLogoutWithReply() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, registeredService.getRealm());
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNOUT10);

        assertDoesNotThrow(() -> {
            federationValidateRequestController.handleFederationRequest(response, request);
            return null;
        });
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        assertEquals("https://cas.example.org:8443/cas/logout?service=http://app.example5.org/wsfed-idp",
            response.getHeader("Location"));
    }

    @Test
    public void verifyLogoutWithoutReply() {
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
    public void verifyLogin() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, registeredService.getRealm());
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);

        assertDoesNotThrow(() -> {
            federationValidateRequestController.handleFederationRequest(response, request);
            return null;
        });
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        assertEquals("https://cas.example.org:8443/cas/login?"
                + "service=%2Fws%2Fidp%2Ffederationcallback%3Fwa%3Dwsignin1.0%26wreply%3Dhttp%253A%252F%252Fapp.example5"
                + ".org%252Fwsfed-idp%26wtrealm%3Durn%253Aorg%253Aapereo%253Acas%253Aws%253Aidp%253Arealm-CAS",
            response.getHeader("Location"));
    }

    @Test
    public void verifyLoginRenewWithNoToken() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val registeredService = getWsFederationRegisteredService();
        request.addParameter(WSFederationConstants.WTREALM, registeredService.getRealm());
        request.addParameter(WSFederationConstants.WREPLY, registeredService.getServiceId());
        request.addParameter(WSFederationConstants.WREFRESH, "5000");
        request.addParameter(WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        assertDoesNotThrow(() -> {
            federationValidateRequestController.handleFederationRequest(response, request);
            return null;
        });
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        assertEquals("https://cas.example.org:8443/cas/login?service="
                + "%2Fws%2Fidp%2Ffederationcallback%3Fwa%3Dwsignin1.0%26wreply%3Dhttp%253A%252F%252Fapp.example5"
                + ".org%252Fwsfed-idp%26wtrealm%3Durn%253Aorg%253Aapereo%253Acas%253Aws%253Aidp%253Arealm-CAS%26wfresh%3D5000&renew=true",
            response.getHeader("Location"));
    }

    @Test
    public void verifyLoginRenewWithToken() {
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

        val id = UUID.randomUUID().toString();
        val sts = mock(SecurityTokenTicket.class);
        when(sts.getPrefix()).thenReturn(SecurityTokenTicket.PREFIX);
        when(sts.getId()).thenReturn(SecurityTokenTicket.PREFIX + '-' + id);
        when(sts.isExpired()).thenReturn(Boolean.FALSE);
        when(sts.getSecurityToken()).thenReturn(token);

        ticketRegistry.addTicket(sts);

        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.getDescendantTickets().add(sts.getId());
        ticketRegistry.addTicket(tgt);
        ticketGrantingTicketCookieGenerator.addCookie(response, tgt.getId());
        request.setCookies(response.getCookies());

        assertDoesNotThrow(() -> {
            federationValidateRequestController.handleFederationRequest(response, request);
            return null;
        });
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        assertEquals("https://cas.example.org:8443/cas/login?service="
                + "%2Fws%2Fidp%2Ffederationcallback%3Fwa%3Dwsignin1.0%26wreply%3Dhttp%253A%252F%252Fapp.example5"
                + ".org%252Fwsfed-idp%26wtrealm%3Durn%253Aorg%253Aapereo%253Acas%253Aws%253Aidp%253Arealm-CAS%26wfresh%3D1&renew=true",
            response.getHeader("Location"));
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

}
