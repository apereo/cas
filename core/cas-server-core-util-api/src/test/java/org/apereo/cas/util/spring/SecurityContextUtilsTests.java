package org.apereo.cas.util.spring;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SecurityContextUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Utility")
class SecurityContextUtilsTests {
    @Test
    void verifyOperation() throws Exception {
        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("id");
        when(principal.getAttributes()).thenReturn(Map.of("name", List.of("CAS")));
        val context = SecurityContextUtils.createSecurityContext(principal, new MockHttpServletRequest());
        assertTrue(context.getAuthentication().isAuthenticated());
        assertEquals("id", context.getAuthentication().getName());
    }

    @Test
    void verifyTicketOperation() throws Exception {
        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("id");
        when(principal.getAttributes()).thenReturn(Map.of("name", List.of("CAS")));

        val ticket = mock(TransientSessionTicket.class);
        when(ticket.getProperty(Principal.class.getName(), Principal.class)).thenReturn(principal);
        
        val context = SecurityContextUtils.createSecurityContext(ticket, new MockHttpServletRequest());
        assertTrue(context.getAuthentication().isAuthenticated());
        assertEquals("id", context.getAuthentication().getName());
    }
}
