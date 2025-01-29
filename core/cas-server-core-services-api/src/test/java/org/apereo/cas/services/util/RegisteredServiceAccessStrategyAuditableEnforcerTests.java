package org.apereo.cas.services.util;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
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
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class RegisteredServiceAccessStrategyAuditableEnforcerTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    private AuditableExecutionResult executeAccessStrategy(final AuditableContext context) {
        return new RegisteredServiceAccessStrategyAuditableEnforcer(applicationContext,
            new DefaultRegisteredServicePrincipalAccessStrategyEnforcer(applicationContext)).execute(context);
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

    private static Authentication createAuthentication() {
        val attributes = CoreAuthenticationTestUtils.getAttributes();
        attributes.put("attribute", "value");
        return CoreAuthenticationTestUtils.getAuthentication("principal", attributes);
    }

    private static TicketGrantingTicket createTicketGrantingTicket() {
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

    private static AuthenticationResult createAuthenticationResult() {
        val authentication = createAuthentication();
        return CoreAuthenticationTestUtils.getAuthenticationResult(authentication);
    }

    private static Map<String, Set<String>> reject(final boolean fail) {
        val reject = new HashMap<String, Set<String>>();
        reject.put("attribute", Set.of(fail ? "other_value" : "value"));
        return reject;
    }
    
    @Test
    void verifyRegisteredServicePresentAndEnabled() {
        val service = createRegisteredService(true);
        val context = AuditableContext.builder().registeredService(service).build();
        val result = executeAccessStrategy(context);
        assertFalse(result.isExecutionFailure());
        assertFalse(result.getException().isPresent());
    }

    @Test
    void verifyRegisteredServicePresentButDisabled() {
        val service = createRegisteredService(false);
        val context = AuditableContext.builder().registeredService(service).build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }

    @Test
    void verifyServiceAndRegisteredServicePresentAndEnabled() {
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
    void verifyServiceAndRegisteredServicePresentButDisabled() {
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
    void verifyAuthAndServiceAndRegisteredServicePresentAndEnabled() {
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
    void verifyAuthAndServiceAndRegisteredServicePresentButDisabled() {
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
    void verifyRejectedPrincipalAttributes() {
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
    void verifyRejectedPrincipalAttributesNoFail() {
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
    void verifyTgtAndServiceAndRegisteredServicePresentAndEnabled() {
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
    void verifyTgtAndServiceAndRegisteredServicePresentButDisabled() {
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
    void verifyTgtRejectedPrincipalAttributes() {
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
    void verifyTgtRejectedPrincipalAttributesNoFail() {
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
    void verifyStAndServiceAndRegisteredServicePresentAndEnabled() {
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
    void verifyStAndServiceAndRegisteredServicePresentButDisabled() {
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
    void verifyStRejectedPrincipalAttributes() {
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
    void verifyStRejectedPrincipalAttributesNoFail() {
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
    void verifyExceptionNotThrown() {
        val context = AuditableContext.builder().build();
        val result = executeAccessStrategy(context);
        assertTrue(result.isExecutionFailure());
        assertTrue(result.getException().isPresent());
    }
}
