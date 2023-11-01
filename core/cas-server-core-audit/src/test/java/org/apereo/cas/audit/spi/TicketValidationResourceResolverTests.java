package org.apereo.cas.audit.spi;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.validation.Assertion;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketValidationResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Audits")
@SpringBootTest(classes = BaseAuditConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "cas.audit.engine.include-validation-assertion=true",
        "cas.audit.engine.audit-format=JSON"
    })
class TicketValidationResourceResolverTests {

    @Autowired
    @Qualifier("ticketValidationResourceResolver")
    private AuditResourceResolver ticketValidationResourceResolver;

    @Test
    void verifyActionPassedJson() throws Throwable {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(ArrayUtils.EMPTY_OBJECT_ARRAY);
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());

        assertTrue(ticketValidationResourceResolver.resolveFrom(jp, assertion).length > 0);
    }

    @Test
    void verifyActionPassed() throws Throwable {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(ArrayUtils.EMPTY_OBJECT_ARRAY);
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        assertTrue(ticketValidationResourceResolver.resolveFrom(jp, assertion).length > 0);
    }

    @Test
    void verifyTicketId() throws Throwable {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"ticket-id"});
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        assertTrue(ticketValidationResourceResolver.resolveFrom(jp, assertion).length > 0);
    }

    @Test
    void verifyTicketIdJson() throws Throwable {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"ticket-id"});
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        assertTrue(ticketValidationResourceResolver.resolveFrom(jp, assertion).length > 0);
    }

    @Test
    void verifyEmpty() throws Throwable {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(ArrayUtils.EMPTY_OBJECT_ARRAY);
        assertEquals(0, ticketValidationResourceResolver.resolveFrom(jp, new Object()).length);
    }
}
