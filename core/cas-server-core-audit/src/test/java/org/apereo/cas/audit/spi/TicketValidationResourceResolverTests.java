package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.spi.resource.TicketValidationResourceResolver;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.validation.Assertion;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

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
        when(jp.getArgs()).thenReturn(ArrayUtils.EMPTY_OBJECT_ARRAY);
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        assertTrue(r.resolveFrom(jp, assertion).length > 0);
    }
}
