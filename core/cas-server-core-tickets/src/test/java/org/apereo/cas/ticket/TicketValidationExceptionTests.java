package org.apereo.cas.ticket;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TicketValidationExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
public class TicketValidationExceptionTests {
    @Test
    @SuppressWarnings("DeadException")
    public void verifyOperation() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                new AbstractTicketValidationException(RegisteredServiceTestUtils.getService()) {
                    private static final long serialVersionUID = 3026378968544530445L;
                };
                new AbstractTicketValidationException("code", RegisteredServiceTestUtils.getService()) {
                    private static final long serialVersionUID = 3026378968544530445L;
                };
                new AbstractTicketValidationException("code", "message", List.of(), RegisteredServiceTestUtils.getService()) {
                    private static final long serialVersionUID = 3026378968544530445L;
                };
                new AbstractTicketValidationException("code", new RuntimeException(), List.of(), RegisteredServiceTestUtils.getService()) {
                    private static final long serialVersionUID = 3026378968544530445L;
                };
            }
        });

    }

}
