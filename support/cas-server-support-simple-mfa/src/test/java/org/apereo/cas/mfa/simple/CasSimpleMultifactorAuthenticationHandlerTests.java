package org.apereo.cas.mfa.simple;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import javax.security.auth.login.FailedLoginException;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasSimpleMultifactorAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFA")
public class CasSimpleMultifactorAuthenticationHandlerTests {
    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationHandler")
    private AuthenticationHandler casSimpleMultifactorAuthenticationHandler;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private TicketFactory defaultTicketFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Test
    public void verifyFailsToFindToken() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);

        val id = UUID.randomUUID().toString();
        val credential = new CasSimpleMultifactorTokenCredential(id);
        assertThrows(InvalidTicketException.class, () -> casSimpleMultifactorAuthenticationHandler.authenticate(credential));
    }

    @Test
    public void verifyFailsPrincipal() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);

        val factory = (CasSimpleMultifactorAuthenticationTicketFactory) defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService(), Map.of());
        ticketRegistry.addTicket(ticket);
        val credential = new CasSimpleMultifactorTokenCredential(ticket.getId());
        assertThrows(FailedLoginException.class, () -> casSimpleMultifactorAuthenticationHandler.authenticate(credential));
    }

    @Test
    public void verifyFailsExpiredToken() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val principal = RegisteredServiceTestUtils.getPrincipal();
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(principal), context);

        val factory = (CasSimpleMultifactorAuthenticationTicketFactory) defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService(),
            Map.of(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal));
        ticketRegistry.addTicket(ticket);
        val credential = new CasSimpleMultifactorTokenCredential(ticket.getId());
        ticket.markTicketExpired();

        val centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.getTicket(ArgumentMatchers.eq(ticket.getId()), any())).thenReturn(ticket);
        val handler = new CasSimpleMultifactorAuthenticationHandler(getClass().getSimpleName(),
            servicesManager, PrincipalFactoryUtils.newPrincipalFactory(), centralAuthenticationService, 0);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(credential));
    }
}
