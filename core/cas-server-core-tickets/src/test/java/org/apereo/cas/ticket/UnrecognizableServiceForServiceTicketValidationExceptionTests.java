package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link UnrecognizableServiceForServiceTicketValidationException}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Tickets")
public class UnrecognizableServiceForServiceTicketValidationExceptionTests {
    private final Service service = RegisteredServiceTestUtils.getService();

    @Test
    public void verifyThrowableConstructor() {
        val t =
            new UnrecognizableServiceForServiceTicketValidationException(this.service);

        assertSame(UnrecognizableServiceForServiceTicketValidationException.CODE, t.getCode());
        assertEquals(this.service, t.getService());
    }
}
