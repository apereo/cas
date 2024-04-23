package org.apereo.cas.mfa.simple;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.validation.DefaultCasSimpleMultifactorAuthenticationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.DirectObjectProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
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
@Tag("MFAProvider")
class CasSimpleMultifactorAuthenticationHandlerTests {
    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationHandler")
    private AuthenticationHandler casSimpleMultifactorAuthenticationHandler;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    private TicketFactory defaultTicketFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyFailsToFindToken() throws Throwable {
        val id = UUID.randomUUID().toString();
        val credential = new CasSimpleMultifactorTokenCredential(id);
        assertThrows(FailedLoginException.class,
            () -> casSimpleMultifactorAuthenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyFailsPrincipal() throws Throwable {
        val factory = (CasSimpleMultifactorAuthenticationTicketFactory) defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService(), Map.of());
        ticketRegistry.addTicket(ticket);
        val credential = new CasSimpleMultifactorTokenCredential(ticket.getId());
        assertThrows(FailedLoginException.class, () -> casSimpleMultifactorAuthenticationHandler.authenticate(credential, mock(Service.class)));
        assertFalse(casSimpleMultifactorAuthenticationHandler.supports(new UsernamePasswordCredential()));
        assertFalse(casSimpleMultifactorAuthenticationHandler.supports(UsernamePasswordCredential.class));
    }

    @Test
    void verifyFailsExpiredToken() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal();

        val factory = (CasSimpleMultifactorAuthenticationTicketFactory) defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService(),
            Map.of(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal));
        ticketRegistry.addTicket(ticket);
        val credential = new CasSimpleMultifactorTokenCredential(ticket.getId());
        ticket.markTicketExpired();

        val mfaService = new DefaultCasSimpleMultifactorAuthenticationService(ticketRegistry, defaultTicketFactory);
        val handler = new CasSimpleMultifactorAuthenticationHandler(casProperties.getAuthn().getMfa().getSimple(),
            applicationContext, servicesManager, PrincipalFactoryUtils.newPrincipalFactory(), mfaService,
            new DirectObjectProvider<>(mock(MultifactorAuthenticationProvider.class)));
        assertThrows(FailedLoginException.class, () -> handler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifySuccessfulAuthenticationWithTokenWithoutPrefix() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal();

        val factory = (CasSimpleMultifactorAuthenticationTicketFactory) defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService(),
            Map.of(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal));
        ticketRegistry.addTicket(ticket);
        val ticketIdWithoutPrefix = ticket.getId().substring(CasSimpleMultifactorAuthenticationTicket.PREFIX.length() + 1);
        val credential = new CasSimpleMultifactorTokenCredential(ticketIdWithoutPrefix);
        assertNotNull(casSimpleMultifactorAuthenticationHandler.authenticate(credential, mock(Service.class)).getPrincipal());
    }
}
