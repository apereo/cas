package org.jasig.cas.ticket;

import org.jasig.cas.authentication.principal.Service;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link UnrecognizableServiceForServiceTicketValidationException}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class UnrecognizableServiceForServiceTicketValidationExceptionTests {

    private final Service service = org.jasig.cas.services.TestUtils.getService();

    @Test
    public void verifyThrowableConstructor() {
        final UnrecognizableServiceForServiceTicketValidationException t =
                new UnrecognizableServiceForServiceTicketValidationException(this.service);

        assertSame(UnrecognizableServiceForServiceTicketValidationException.CODE, t.getCode());
        assertEquals(this.service, t.getOriginalService());
    }
}
