package org.apereo.cas.ticket.registry.pubsub.commands;

import org.apereo.cas.ticket.registry.pubsub.QueueableTicketRegistry;
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

    private final PublisherIdentifier publisherIdentifier;

    /**
     * Execute.
     *
     * @param registry the registry
     * @throws Exception the exception
     */
    public abstract void execute(QueueableTicketRegistry registry) throws Exception;
}
