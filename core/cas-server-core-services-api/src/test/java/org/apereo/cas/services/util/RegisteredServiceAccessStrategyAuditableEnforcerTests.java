package org.apereo.cas.services.util;

import lombok.val;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Class to test {@link RegisteredServiceAccessStrategyAuditableEnforcer}.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
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
    public void verifyExceptionThrown() {
        val context = AuditableContext.builder().build();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new RegisteredServiceAccessStrategyAuditableEnforcer().execute(context));
    }

    private RegexRegisteredService createRegisteredService(final boolean enabled) {
        val service = new RegexRegisteredService();
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setEnabled(enabled);
        service.setAccessStrategy(accessStrategy);
        return service;
    }

    private Service createService() {
        return new Service() {
            @Override
            public String getId() {
                return "serviceid";
            }
        };
    }

    private Authentication createAuthentication() {
        val principal = createPrincipal();
        val attributes = new HashMap<String, Object>();
        attributes.put("attribute", "value");
        val successes = new HashMap<String, AuthenticationHandlerExecutionResult>();
        successes.put("success1", createSuccess(principal));
        return new DefaultAuthentication(
                ZonedDateTime.now(),
                principal,
                attributes,
                successes
        );
    }

    private Principal createPrincipal() {
        return new Principal() {
            @Override
            public String getId() {
                return "principal";
            }
        };
    }

    private AuthenticationHandlerExecutionResult createSuccess(final Principal principal) {
        val success = new DefaultAuthenticationHandlerExecutionResult();
        success.setPrincipal(principal);
        return success;
    }

    private TicketGrantingTicket createTicketGrantingTicket() {
        val mock = Mockito.mock(TicketGrantingTicket.class);
        when(mock.getAuthentication()).thenReturn(createAuthentication());
        when(mock.isRoot()).thenReturn(true);
        when(mock.getRoot()).thenReturn(mock);
        return mock;
    }

    private ServiceTicket createServiceTicket() {
        val mock = mock(ServiceTicket.class);
        when(mock.getService()).thenReturn(createService());
        return mock;
    }

    private AuthenticationResult createAuthenticationResult() {
        val mock = mock(AuthenticationResult.class);
        when(mock.getAuthentication()).thenReturn(createAuthentication());
        when(mock.getService()).thenReturn(createService());
        return mock;
    }

    private Map<String, Set<String>> reject(final boolean fail) {
        val reject = new HashMap<String, Set<String>>();
        reject.put("attribute", Set.of(fail ? "other_value" : "value"));
        return reject;
    }
}
