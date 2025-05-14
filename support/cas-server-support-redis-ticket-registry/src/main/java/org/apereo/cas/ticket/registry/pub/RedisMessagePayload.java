package org.apereo.cas.ticket.registry.pub;

import org.apereo.cas.util.PublisherIdentifier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link RedisMessagePayload}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
@With
@RequiredArgsConstructor
@ToString
@Jacksonized
public class RedisMessagePayload<T extends Serializable> implements Serializable {
    @Serial
    private static final long serialVersionUID = -2448524640612359787L;

    private final PublisherIdentifier identifier;

    private final RedisMessageTypes messageType;

    private final T ticket;

    public enum RedisMessageTypes {
        /**
         * Message type to add ticket to the cache.
         */
        ADD,
        /**
         * Message type to update ticket in the cache.
         */
        UPDATE,
        /**
         * Message type to delete ticket from the cache.
         */
        DELETE,
        /**
         * Message type to delete all tickets.
         */
        DELETE_ALL
    }
}
