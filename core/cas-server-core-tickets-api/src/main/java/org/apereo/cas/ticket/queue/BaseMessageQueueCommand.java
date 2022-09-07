package org.apereo.cas.ticket.queue;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link BaseMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public abstract class BaseMessageQueueCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 7050449807845156228L;

    private final PublisherIdentifier id;

    /**
     * Execute.
     *
     * @param registry the registry
     * @throws Exception the exception
     */
    public void execute(final TicketRegistry registry) throws Exception {
    }

    protected static Ticket deserializeTicket(final String ticket, final String ticketType) {
        val manager = ApplicationContextProvider.getApplicationContext().getBean(TicketSerializationManager.class);
        return manager.deserializeTicket(ticket, ticketType);
    }
}
