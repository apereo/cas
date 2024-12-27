package org.apereo.cas.audit.spi.principal;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableEntity;
import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAuditPrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAuditConfigurationTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultAuditPrincipalResolverTests {
    @Autowired
    @Qualifier("auditablePrincipalResolver")
    private PrincipalResolver auditablePrincipalResolver;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    
    @ParameterizedTest
    @MethodSource("getAuditParameters")
    void verifyOperation(final Object argument, final Object returnValue) {
        if (argument instanceof final MockRequestContext ctx) {
            ctx.setApplicationContext(applicationContext);
        }
        val jp = mockJoinPointWithFirstArg(argument);
        val principalId = auditablePrincipalResolver.resolveFrom(jp, returnValue);
        assertNotEquals(PrincipalResolver.UNKNOWN_USER, principalId);
    }

    @ParameterizedTest
    @MethodSource("getAuditReturnValueParameters")
    void verifyReturnValue(final Object returnValue) {
        val jp = mockJoinPointWithFirstArg(new Object());
        val principalId = auditablePrincipalResolver.resolveFrom(jp, returnValue);
        assertNotEquals(PrincipalResolver.UNKNOWN_USER, principalId);
    }

    @Test
    void verifySimpleResolve() {
        assertEquals(PrincipalResolver.UNKNOWN_USER, auditablePrincipalResolver.resolve());
    }

    @Test
    void verifyExceptionResolve() {
        val jp = mockJoinPointWithFirstArg(UUID.randomUUID().toString());
        assertEquals(PrincipalResolver.UNKNOWN_USER, auditablePrincipalResolver.resolveFrom(jp, new RuntimeException()));
    }

    @Test
    void verifyUnknownParameter() {
        val jp = mockJoinPointWithFirstArg(UUID.randomUUID().toString());
        assertEquals(PrincipalResolver.UNKNOWN_USER, auditablePrincipalResolver.resolveFrom(jp, new Object()));
    }

    public static Stream<Arguments> getAuditReturnValueParameters() {
        val authentication = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val ticketGrantingTicket = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), authentication, NeverExpiresExpirationPolicy.INSTANCE);
        val authenticationResult = CoreAuthenticationTestUtils.getAuthenticationResult(authentication);
        val auditableContext = AuditableContext.builder()
            .authentication(authentication)
            .authenticationResult(authenticationResult)
            .build();
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);

        val auditableEntity = new AuditableEntity() {
            @Override
            public String getAuditablePrincipal() {
                return UUID.randomUUID().toString();
            }
        };

        return Stream.of(
            arguments(ticketGrantingTicket),
            arguments(auditableContext),
            arguments(ticketGrantingTicket),
            arguments(assertion),
            arguments(auditableEntity)
        );
    }

    public static Stream<Arguments> getAuditParameters() throws Throwable {
        val context = MockRequestContext.create();
        val authentication = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        WebUtils.putAuthentication(authentication, context);

        val credentials = RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword(UUID.randomUUID().toString());
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials);
        val authenticationResult = CoreAuthenticationTestUtils.getAuthenticationResult(authentication);

        val ticketGrantingTicket = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), authentication, NeverExpiresExpirationPolicy.INSTANCE);
        val sloRequest = SingleLogoutExecutionRequest.builder()
            .ticketGrantingTicket(ticketGrantingTicket)
            .build();

        val auditableContext = AuditableContext.builder()
            .authentication(authentication)
            .authenticationResult(authenticationResult)
            .build();

        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);

        val auditableEntity = mock(AuditableEntity.class);
        when(auditableEntity.getAuditablePrincipal()).thenReturn(authentication.getPrincipal().getId());

        val httpRequest = new MockHttpServletRequest();
        httpRequest.setAttribute(Principal.class.getName(), authentication.getPrincipal());
        
        return Stream.of(
            arguments(context, null),
            arguments(sloRequest, null),
            arguments(transaction, null),
            arguments(credentials, null),
            arguments(authenticationResult, null),
            arguments(auditableContext, null),
            arguments(authentication, null),
            arguments(assertion, null),
            arguments(auditableEntity, null),
            arguments(httpRequest, null)
        );
    }

    private static JoinPoint mockJoinPointWithFirstArg(final Object argument) {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{argument});
        return jp;
    }
}
