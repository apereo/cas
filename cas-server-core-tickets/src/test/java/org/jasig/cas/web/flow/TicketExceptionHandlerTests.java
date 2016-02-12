package org.jasig.cas.web.flow;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ContextualAuthenticationPolicy;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.springframework.binding.message.DefaultMessageResolver;
import org.springframework.binding.message.MessageContext;

/**
 * @author Daniel Frett
 * @since 4.3.0
 */
@RunWith(JUnit4.class)
public class TicketExceptionHandlerTests {
    @Test
    public void handleUnknownExceptionByDefault() {
        final TicketExceptionHandler handler = new TicketExceptionHandler();
        final MessageContext ctx = mock(MessageContext.class);

        final String id = handler.handle(new InvalidTicketException("TGT"), ctx);
        assertEquals(id, "UNKNOWN");
        verifyZeroInteractions(ctx);
    }

    @Test
    public void handleUnsatisfiedAuthenticationPolicyExceptionByDefault() {
        final TicketExceptionHandler handler = new TicketExceptionHandler();
        final MessageContext ctx = mock(MessageContext.class);

        final ContextualAuthenticationPolicy<?> policy = new ContextualAuthenticationPolicy<Object>() {
            @Override
            public Object getContext() {
                return null;
            }

            @Override
            public boolean isSatisfiedBy(final Authentication authentication) {
                return false;
            }
        };
        final String id = handler.handle(new UnsatisfiedAuthenticationPolicyException(policy), ctx);
        assertEquals(id, "UnsatisfiedAuthenticationPolicyException");
        ArgumentCaptor<DefaultMessageResolver> message = ArgumentCaptor.forClass(DefaultMessageResolver.class);
        verify(ctx, times(1)).addMessage(message.capture());
        assertArrayEquals(new String[]{"ticketFailure.UnsatisfiedAuthenticationPolicyException"}, message.getValue()
                .getCodes());
    }
}
