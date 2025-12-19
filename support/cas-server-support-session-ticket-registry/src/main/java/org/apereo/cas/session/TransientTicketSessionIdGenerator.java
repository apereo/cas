package org.apereo.cas.session;

import module java.base;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.session.SessionIdGenerator;

/**
 * This is {@link TransientTicketSessionIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class TransientTicketSessionIdGenerator implements SessionIdGenerator {
    private final TransientSessionTicketFactory transientSessionTicketFactory;

    @NonNull
    @Override
    public String generate() {
        return FunctionUtils.doUnchecked(
            () -> transientSessionTicketFactory.getTicketIdGenerator().getNewTicketId(TransientSessionTicket.PREFIX)
        );
    }
}
