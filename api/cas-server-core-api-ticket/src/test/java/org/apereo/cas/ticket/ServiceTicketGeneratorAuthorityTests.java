package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceTicketGeneratorAuthorityTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Tickets")
class ServiceTicketGeneratorAuthorityTests {
    @Test
    void verifyAllow() throws Throwable {
        val allow = ServiceTicketGeneratorAuthority.allow();
        assertTrue(allow.supports(mock(AuthenticationResult.class), mock(Service.class)));
        assertTrue(allow.shouldGenerate(mock(AuthenticationResult.class), mock(Service.class)));
        assertEquals(Ordered.LOWEST_PRECEDENCE, allow.getOrder());
    }
}
