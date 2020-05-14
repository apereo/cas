package org.apereo.cas.services.util;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
@Tag("Simple")
public class RegisteredServiceAccessStrategyAuditableEnforcerTests {

    @Test
    public void verifyRegisteredServicePresentAndEnabled() {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder().registeredService(service).build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    public void verifyRegisteredServicePresentButDisabled() {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder().registeredService(service).build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    public void verifyServiceAndRegisteredServicePresentAndEnabled() {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    public void verifyServiceAndRegisteredServicePresentButDisabled() {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    public void verifyAuthAndServiceAndRegisteredServicePresentAndEnabled() {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .authentication(createAuthentication())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    public void verifyAuthAndServiceAndRegisteredServicePresentButDisabled() {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .authentication(createAuthentication())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    public void verifyRejectedPrincipalAttributes() {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(false));
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .authentication(createAuthentication())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    public void verifyRejectedPrincipalAttributesNoFail() {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(true));
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .authentication(createAuthentication())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    public void verifyTgtAndServiceAndRegisteredServicePresentAndEnabled() {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .ticketGrantingTicket(createTicketGrantingTicket())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    public void verifyTgtAndServiceAndRegisteredServicePresentButDisabled() {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .ticketGrantingTicket(createTicketGrantingTicket())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    public void verifyTgtRejectedPrincipalAttributes() {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(false));
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .ticketGrantingTicket(createTicketGrantingTicket())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    public void verifyTgtRejectedPrincipalAttributesNoFail() {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(true));
        val context = AuditableContext.builder()
            .registeredService(service)
            .service(createService())
            .ticketGrantingTicket(createTicketGrantingTicket())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    public void verifyStAndServiceAndRegisteredServicePresentAndEnabled() {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder()
            .registeredService(service)
            .serviceTicket(createServiceTicket())
            .authenticationResult(createAuthenticationResult())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    public void verifyStAndServiceAndRegisteredServicePresentButDisabled() {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder()
            .registeredService(service)
            .serviceTicket(createServiceTicket())
            .authenticationResult(createAuthenticationResult())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    public void verifyStRejectedPrincipalAttributes() {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(false));
        val context = AuditableContext.builder()
            .registeredService(service)
            .serviceTicket(createServiceTicket())
            .authenticationResult(createAuthenticationResult())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    public void verifyStRejectedPrincipalAttributesNoFail() {
        val service = createRegisteredService(true);
        ((DefaultRegisteredServiceAccessStrategy) service.getAccessStrategy()).setRejectedAttributes(reject(true));
        val context = AuditableContext.builder()
            .registeredService(service)
            .serviceTicket(createServiceTicket())
            .authenticationResult(createAuthenticationResult())
            .build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    public void verifyExceptionNotThrown() {
        val context = AuditableContext.builder().build();
        val result = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    private static RegexRegisteredService createRegisteredService(final boolean enabled) {
        val service = new RegexRegisteredService();
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setEnabled(enabled);
        service.setAccessStrategy(accessStrategy);
        return service;
    }

    private static Service createService() {
        return CoreAuthenticationTestUtils.getService("serviceid");
    }

    private static Authentication createAuthentication() {
        val attributes = CoreAuthenticationTestUtils.getAttributes();
        attributes.put("attribute", "value");
        return CoreAuthenticationTestUtils.getAuthentication("principal", attributes);
    }

    private static TicketGrantingTicket createTicketGrantingTicket() {
        val mock = Mockito.mock(TicketGrantingTicket.class);
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

    private static AuthenticationResult createAuthenticationResult() {
        val authentication = createAuthentication();
        return CoreAuthenticationTestUtils.getAuthenticationResult(authentication);
    }

    private static Map<String, Set<String>> reject(final boolean fail) {
        val reject = new HashMap<String, Set<String>>();
        reject.put("attribute", Set.of(fail ? "other_value" : "value"));
        return reject;
    }
}
