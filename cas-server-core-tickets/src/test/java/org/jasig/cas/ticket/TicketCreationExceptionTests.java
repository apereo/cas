package org.jasig.cas.ticket;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class TicketCreationExceptionTests {

    @Test
    public void verifyNoParamConstructor() {
        new TicketCreationException();
    }

    @Test
    public void verifyThrowableParamConstructor() {
        final Throwable throwable = new Throwable();
        final TicketCreationException t = new TicketCreationException(throwable);

        assertEquals(throwable, t.getCause());
    }
}
