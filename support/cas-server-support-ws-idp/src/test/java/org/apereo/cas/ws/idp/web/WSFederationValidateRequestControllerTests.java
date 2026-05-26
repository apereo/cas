package org.apereo.cas.ws.idp.web;

import module java.base;
import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import lombok.val;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyNoWa() throws Throwable {
        val result = performFederationRequest();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertNotNull(result.getModelAndView());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, result.getModelAndView().getViewName());
        assertInstanceOf(UnauthorizedAuthenticationException.class,
            result.getModelAndView().getModel().get(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION));
    }

    @Test
    void verifyLogoutWithReply() throws Throwable {
        val registeredService = getWsFederationRegisteredService();
        val result = performFederationRequest(
            WSFederationConstants.WTREALM, registeredService.getRealm(),
            WSFederationConstants.WREPLY, registeredService.getServiceId(),
            WSFederationConstants.WA, WSFederationConstants.WSIGNOUT10,
            WSFederationConstants.WHR, "whr",
            WSFederationConstants.WREQ, "wreq");
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertEquals("https://cas.example.org:8443/cas/logout?service=http://app.example5.org/wsfed-idp", result.getResponse().getHeader("Location"));
    }

    @Test
    void verifyUnauthzServicesWithUnknownRealm() throws Throwable {
        val registeredService = getWsFederationRegisteredService();
        val result = performFederationRequest(
            WSFederationConstants.WTREALM, "unknown",
            WSFederationConstants.WREPLY, registeredService.getServiceId(),
            WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertNotNull(result.getModelAndView());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, result.getModelAndView().getViewName());
    }

    @Test
    void verifyUnauthzServicesWithMismatchedRealm() throws Throwable {
        val registeredService = getWsFederationRegisteredService("custom-realm");
        val result = performFederationRequest(
            WSFederationConstants.WTREALM, "custom-realm",
            WSFederationConstants.WREPLY, registeredService.getServiceId(),
            WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertNotNull(result.getModelAndView());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, result.getModelAndView().getViewName());
    }


    @Test
    void verifyLogoutWithoutReply() throws Throwable {
        val result = performFederationRequest(WSFederationConstants.WA, WSFederationConstants.WSIGNOUT10);
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertEquals("https://cas.example.org:8443/cas/logout", result.getResponse().getHeader("Location"));
    }

    @Test
    void verifyLogin() throws Throwable {
        val registeredService = getWsFederationRegisteredService();
        val result = performFederationRequest(
            WSFederationConstants.WTREALM, registeredService.getRealm(),
            WSFederationConstants.WREPLY, registeredService.getServiceId(),
            WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());

        val builder = new URIBuilder(result.getResponse().getHeader("Location"));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_SERVICE)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WTREALM)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WREPLY)));
    }

    @Test
    void verifyLoginRenewWithNoToken() throws Throwable {
        val registeredService = getWsFederationRegisteredService();
        val result = performFederationRequest(
            WSFederationConstants.WTREALM, registeredService.getRealm(),
            WSFederationConstants.WREPLY, registeredService.getServiceId(),
            WSFederationConstants.WREFRESH, "5000",
            WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        val builder = new URIBuilder(result.getResponse().getHeader("Location"));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_SERVICE)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_RENEW)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WTREALM)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WREPLY)));
    }

    @Test
    void verifyLoginRenewDisabled() throws Throwable {
        val registeredService = getWsFederationRegisteredService();
        val result = performFederationRequest(
            WSFederationConstants.WTREALM, registeredService.getRealm(),
            WSFederationConstants.WREPLY, registeredService.getServiceId(),
            WSFederationConstants.WREFRESH, "0",
            WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        val builder = new URIBuilder(result.getResponse().getHeader("Location"));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_SERVICE)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WTREALM)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WREPLY)));
    }

    @Test
    void verifyLoginRenewWithToken() throws Throwable {
        val registeredService = getWsFederationRegisteredService();
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

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        ticketGrantingTicketCookieGenerator.addCookie(request, response, tgt.getId());
        val result = performFederationRequest(response.getCookies(),
            WSFederationConstants.WTREALM, registeredService.getRealm(),
            WSFederationConstants.WREPLY, registeredService.getServiceId(),
            WSFederationConstants.WREFRESH, "1",
            WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        val builder = new URIBuilder(result.getResponse().getHeader("Location"));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_SERVICE)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(CasProtocolConstants.PARAMETER_RENEW)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WTREALM)));
        assertTrue(builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(WSFederationConstants.WREPLY)));
    }

    private MvcResult performFederationRequest(final String... parameters) throws Throwable {
        return performFederationRequest(null, parameters);
    }

    private MvcResult performFederationRequest(final Cookie[] cookies, final String... parameters) throws Throwable {
        val builder = get(WSFederationConstants.ENDPOINT_FEDERATION_REQUEST);
        if (cookies != null && cookies.length > 0) {
            builder.cookie(cookies);
        }
        for (var i = 0; i < parameters.length; i += 2) {
            builder.param(parameters[i], parameters[i + 1]);
        }
        return mockMvc.perform(builder)
            .andReturn();
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
