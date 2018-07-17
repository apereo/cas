package org.apereo.cas.audit.spi;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.validation.Assertion;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketValidationResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class TicketValidationResourceResolverTests {
    private final TicketValidationResourceResolver r = new TicketValidationResourceResolver();

    @Test
    public void verifyActionPassed() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{});
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        assertTrue(r.resolveFrom(jp, assertion).length > 0);
    }
}
