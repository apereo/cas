package org.apereo.cas.ticket.registry.sub;

import org.apereo.cas.ticket.registry.pub.RedisMessagePayload;

/**
 * This is {@link RedisTicketRegistryMessageListener}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface RedisTicketRegistryMessageListener {
    /**
     * Handle message.
     *
     * @param command the command
     * @param topic   the topic
     */
    void handleMessage(RedisMessagePayload command, String topic);
}
