package org.apereo.cas.ticket.registry.queue.commands;

import org.apereo.cas.ticket.registry.AMQPTicketRegistry;
import org.apereo.cas.util.PublisherIdentifier;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

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
@Setter
@With
@RequiredArgsConstructor
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
    public abstract void execute(AMQPTicketRegistry registry) throws Exception;
}
