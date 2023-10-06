package org.apereo.cas.services.util;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Class to test {@link RegisteredServiceAccessStrategyAuditableEnforcer}.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
@Tag("RegisteredService")
class RegisteredServiceAccessStrategyAuditableEnforcerTests {

    private static AuditableExecutionResult executeAccessStrategy(final AuditableContext context) {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        return new RegisteredServiceAccessStrategyAuditableEnforcer(appCtx).execute(context);
    }

    private static RegisteredService createRegisteredService(final boolean enabled) {
        val service = new CasRegisteredService();
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setEnabled(enabled);
        service.setAccessStrategy(accessStrategy);
        return service;
    }

    private static Service createService() {
        return CoreAuthenticationTestUtils.getService("serviceid");
    }

    private static Authentication createAuthentication() throws Throwable {
        val attributes = CoreAuthenticationTestUtils.getAttributes();
        attributes.put("attribute", "value");
        return CoreAuthenticationTestUtils.getAuthentication("principal", attributes);
    }

    private static TicketGrantingTicket createTicketGrantingTicket() throws Throwable {
        val mock = mock(TicketGrantingTicket.class);
        val authentication = createAuthentication();
        when(mock.getAuthentication()).thenReturn(authentication);
        when(mock.isRoot()).thenReturn(true);
        when(mock.getRoot()).thenReturn(mock);
        return mock;
    }

    private static ServiceTicket createServiceTicket() {
        val mock = mock(ServiceTicket.class);
        val service = createService();
        when(mock.getService()).thenReturn(service);
        return mock;
    }

    private static AuthenticationResult createAuthenticationResult() throws Throwable {
        val authentication = createAuthentication();
        return CoreAuthenticationTestUtils.getAuthenticationResult(authentication);
    }

    private static Map<String, Set<String>> reject(final boolean fail) {
        val reject = new HashMap<String, Set<String>>();
        reject.put("attribute", Set.of(fail ? "other_value" : "value"));
        return reject;
    }
    
    @Test
    void verifyRegisteredServicePresentAndEnabled() throws Throwable {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder().registeredService(service).build();
        val result = executeAccessStrategy(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    void verifyRegisteredServicePresentButDisabled() throws Throwable {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder().registeredService(service).build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    void verifyServiceAndRegisteredServicePresentAndEnabled() throws Throwable {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .build();
        val result = executeAccessStrategy(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    void verifyServiceAndRegisteredServicePresentButDisabled() throws Throwable {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    void verifyAuthAndServiceAndRegisteredServicePresentAndEnabled() throws Throwable {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .authentication(createAuthentication())
            .build();
        val result = executeAccessStrategy(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    void verifyAuthAndServiceAndRegisteredServicePresentButDisabled() throws Throwable {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .authentication(createAuthentication())
            .build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    void verifyRejectedPrincipalAttributes() throws Throwable {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(false));
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .authentication(createAuthentication())
            .build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    void verifyRejectedPrincipalAttributesNoFail() throws Throwable {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(true));
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .authentication(createAuthentication())
            .build();
        val result = executeAccessStrategy(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    void verifyTgtAndServiceAndRegisteredServicePresentAndEnabled() throws Throwable {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .ticketGrantingTicket(createTicketGrantingTicket())
            .build();
        val result = executeAccessStrategy(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    void verifyTgtAndServiceAndRegisteredServicePresentButDisabled() throws Throwable {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .ticketGrantingTicket(createTicketGrantingTicket())
            .build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    void verifyTgtRejectedPrincipalAttributes() throws Throwable {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(false));
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .ticketGrantingTicket(createTicketGrantingTicket())
            .build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    void verifyTgtRejectedPrincipalAttributesNoFail() throws Throwable {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(true));
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .ticketGrantingTicket(createTicketGrantingTicket())
            .build();
        val result = executeAccessStrategy(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    void verifyStAndServiceAndRegisteredServicePresentAndEnabled() throws Throwable {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder()
            .registeredService(service)
            .serviceTicket(createServiceTicket())
            .authenticationResult(createAuthenticationResult())
            .build();
        val result = executeAccessStrategy(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    void verifyStAndServiceAndRegisteredServicePresentButDisabled() throws Throwable {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder()
            .registeredService(service)
            .serviceTicket(createServiceTicket())
            .authenticationResult(createAuthenticationResult())
            .build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    void verifyStRejectedPrincipalAttributes() throws Throwable {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(false));
        val context = AuditableContext.builder()
            .registeredService(service)
            .serviceTicket(createServiceTicket())
            .authenticationResult(createAuthenticationResult())
            .build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    void verifyStRejectedPrincipalAttributesNoFail() throws Throwable {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(true));
        val context = AuditableContext.builder()
            .registeredService(service)
            .serviceTicket(createServiceTicket())
            .authenticationResult(createAuthenticationResult())
            .build();
        val result = executeAccessStrategy(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    void verifyExceptionNotThrown() throws Throwable {
        val context = AuditableContext.builder().build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }
}
