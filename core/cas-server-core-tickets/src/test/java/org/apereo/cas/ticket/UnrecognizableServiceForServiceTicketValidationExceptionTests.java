package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link UnrecognizableServiceForServiceTicketValidationException}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class UnrecognizableServiceForServiceTicketValidationExceptionTests {

    private final Service service = RegisteredServiceTestUtils.getService();

    @Test
    public void verifyThrowableConstructor() {
        final UnrecognizableServiceForServiceTicketValidationException t =
                new UnrecognizableServiceForServiceTicketValidationException(this.service);

        assertSame(UnrecognizableServiceForServiceTicketValidationException.CODE, t.getCode());
        assertEquals(this.service, t.getOriginalService());
    }
}
