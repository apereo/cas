package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.spi.resource.TicketValidationResourceResolver;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.validation.Assertion;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketValidationResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Audits")
public class TicketValidationResourceResolverTests {
    @Test
    public void verifyActionPassedJson() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(ArrayUtils.EMPTY_OBJECT_ARRAY);
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());

        val resolver = getResolver();
        assertTrue(resolver.resolveFrom(jp, assertion).length > 0);
    }

    @Test
    public void verifyActionPassed() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(ArrayUtils.EMPTY_OBJECT_ARRAY);
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        val resolver = getResolver();
        assertTrue(resolver.resolveFrom(jp, assertion).length > 0);
    }

    @Test
    public void verifyTicketId() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"ticket-id"});
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        val resolver = getResolver();
        assertTrue(resolver.resolveFrom(jp, assertion).length > 0);
    }

    @Test
    public void verifyTicketIdJson() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"ticket-id"});
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        val resolver = getResolver();
        assertTrue(resolver.resolveFrom(jp, assertion).length > 0);
    }

    @Test
    public void verifyEmpty() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(ArrayUtils.EMPTY_OBJECT_ARRAY);
        val resolver = getResolver();
        assertEquals(0, resolver.resolveFrom(jp, new Object()).length);
    }

    @Nonnull
    private static AuditResourceResolver getResolver() {
        return new TicketValidationResourceResolver(
            new AuditEngineProperties().setAuditFormat(AuditEngineProperties.AuditFormatTypes.JSON));
    }
}
