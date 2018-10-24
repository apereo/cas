package org.apereo.cas.audit.spi.principal;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * This is {@link ChainingAuditPrincipalIdProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ChainingAuditPrincipalIdProviderTests {
    @Test
    public void verifyOperation() {
        val chain = new ChainingAuditPrincipalIdProvider(new ArrayList<>());
        chain.addProvider(new DefaultAuditPrincipalIdProvider());
        assertTrue(chain.supports(RegisteredServiceTestUtils.getAuthentication(), new Object(), null));
        val principal = chain.getPrincipalIdFrom(RegisteredServiceTestUtils.getAuthentication(), new Object(), null);
        assertEquals("test", principal);
    }
}
