package org.apereo.cas.audit.spi.principal;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingAuditPrincipalIdProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Audits")
public class ChainingAuditPrincipalIdProviderTests {
    @Test
    public void verifyOperation() {
        val chain = new ChainingAuditPrincipalIdProvider(new ArrayList<>());
        chain.addProvider(new DefaultAuditPrincipalIdProvider());
        assertTrue(chain.supports(mock(JoinPoint.class),
            RegisteredServiceTestUtils.getAuthentication(), new Object(), null));
        val principal = chain.getPrincipalIdFrom(mock(JoinPoint.class),
            RegisteredServiceTestUtils.getAuthentication(), new Object(), null);
        assertEquals("test", principal);
    }

    @Test
    public void verifyAll() {
        val chain = new ChainingAuditPrincipalIdProvider(new ArrayList<>());
        chain.addProviders(List.of(new DefaultAuditPrincipalIdProvider()));
        assertTrue(chain.supports(mock(JoinPoint.class), RegisteredServiceTestUtils.getAuthentication(), new Object(), null));
        val principal = chain.getPrincipalIdFrom(mock(JoinPoint.class), RegisteredServiceTestUtils.getAuthentication(), new Object(), null);
        assertEquals("test", principal);
        assertEquals(Integer.MAX_VALUE, chain.getOrder());
    }
}
