package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ServiceResourceResolverTests {
    @Test
    public void verifyOperation() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"something", RegisteredServiceTestUtils.getService()});
        val resolver = new ServiceResourceResolver();
        var input = resolver.resolveFrom(jp, null);
        assertTrue(input.length > 0);

        input = resolver.resolveFrom(jp, new RuntimeException());
        assertTrue(input.length > 0);
    }
}
