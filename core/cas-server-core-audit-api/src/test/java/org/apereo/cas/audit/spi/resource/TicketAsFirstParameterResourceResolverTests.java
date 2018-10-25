package org.apereo.cas.audit.spi.resource;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketAsFirstParameterResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class TicketAsFirstParameterResourceResolverTests {
    @Test
    public void verifyOperation() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"ST-123434"});
        val resolver = new TicketAsFirstParameterResourceResolver();
        val input = resolver.resolveFrom(jp, null);
        assertTrue(input.length > 0);
    }
}
