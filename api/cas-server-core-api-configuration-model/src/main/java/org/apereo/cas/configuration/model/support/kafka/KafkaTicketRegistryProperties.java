package org.apereo.cas.configuration.model.support.kafka;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;

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
     * In Kafka, a group ID is a unique identifier for a group of consumers.
     * Consumers within the same group share the same group ID. The group
     * ID is used to coordinate the consumption of messages
     * from Kafka topics. Here are some key points about group IDs
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
