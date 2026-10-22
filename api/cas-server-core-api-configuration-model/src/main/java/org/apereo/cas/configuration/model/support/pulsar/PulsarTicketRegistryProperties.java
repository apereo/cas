package org.apereo.cas.configuration.model.support.pulsar;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link PulsarTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-pulsar-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class PulsarTicketRegistryProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = 2222012359713238647L;

    /**
     * Prefix of the subscription each CAS node uses to receive ticket registry messages.
     * Every node must see every change, so a node subscribes under this prefix followed
     * by its queue identifier ({@code cas.ticket.registry.core.queue-identifier}), giving
     * each node a subscription of its own. Subscriptions are durable: give every node a
     * stable queue identifier, otherwise each restart leaves an abandoned subscription
     * behind that the broker keeps retaining messages for.
     */
    private String subscriptionName = "cas-pulsar-ticket-registry-subscription";

    /**
     * Number of consumers that receive ticket registry messages concurrently on each node.
     */
    private int concurrency = 1;
}
