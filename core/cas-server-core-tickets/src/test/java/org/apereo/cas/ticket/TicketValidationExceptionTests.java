package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TicketValidationExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
class TicketValidationExceptionTests {
    @Test
    @SuppressWarnings("DeadException")
    void verifyOperation() {
        assertDoesNotThrow(() -> {
            new AbstractTicketValidationException(RegisteredServiceTestUtils.getService()) {
                @Serial
                private static final long serialVersionUID = 3026378968544530445L;
            };
            new AbstractTicketValidationException("code", RegisteredServiceTestUtils.getService()) {
                @Serial
                private static final long serialVersionUID = 3026378968544530445L;
            };
            new AbstractTicketValidationException("code", "message", List.of(), RegisteredServiceTestUtils.getService()) {
                @Serial
                private static final long serialVersionUID = 3026378968544530445L;
            };
            new AbstractTicketValidationException("code", new RuntimeException(), List.of(), RegisteredServiceTestUtils.getService()) {
                @Serial
                private static final long serialVersionUID = 3026378968544530445L;
            };
        });

    }

}
