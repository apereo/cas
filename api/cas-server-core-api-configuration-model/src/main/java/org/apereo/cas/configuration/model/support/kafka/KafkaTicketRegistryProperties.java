package org.apereo.cas.configuration.model.support.kafka;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link KafkaTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-kafka-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class KafkaTicketRegistryProperties extends BaseKafkaProperties {
    @Serial
    private static final long serialVersionUID = -7556702787447878134L;
    
    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto =
        new EncryptionRandomizedSigningJwtCryptographyProperties().setEnabled(false);

    /**
     * Whether the registry should auto-create topics.
     */
    private boolean autoCreateTopics = true;

    /**
     * Prefix of the consumer group each CAS node uses to consume ticket registry messages.
     * Every node keeps its own copy of the registry and must receive every change, so the
     * group is completed with the node's queue identifier ({@code cas.ticket.registry.core.queue-identifier},
     * or a random value when that is not set) and no two nodes share a group. Nodes that share
     * a group split the topic partitions between them, so each change would reach only one of them.
     * Setting the queue identifier keeps a node's group, and its committed offsets, stable across restarts.
     */
    private String groupId = "cas-ticket-registry";

    /**
     * The concurrency level in ConcurrentMessageListenerContainer determines
     * the number of concurrent threads that will be used to process messages.
     * This allows you to parallelize message consumption, which can
     * improve throughput and performance.
     * By default, this is the number of available processors (CPU cores) to the Java virtual machine.
     */
    private int concurrency = Runtime.getRuntime().availableProcessors();
}
