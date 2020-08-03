package org.apereo.cas.configuration.model.support.kafka;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link KafkaSingleTopicProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-kafka-core")
@Getter
@Setter
@Accessors(chain = true)
public class KafkaSingleTopicProperties implements Serializable {
    private static final long serialVersionUID = -1844529231331941592L;

    /**
     * Set the name of the topic.
     */
    private String name;

    /**
     * Set the number of partitions (default 1).
     */
    private int partitions = 1;

    /**
     * Set the number of replicas (default 1).
     */
    private int replicas = 1;

    /**
     * Specify the final compression type for a given topic. This configuration accepts
     * the standard compression codecs ({@code 'gzip', 'snappy', 'lz4', 'zstd'}). It additionally
     * accepts 'uncompressed' which is equivalent to no compression; and 'producer'
     * which means retain the original compression codec set by the producer.
     */
    private String compressionType = "gzip";

    /**
     * Additional configuration options,
     * as pointed out by {@code TopicConfig}.
     */
    private Map<String, String> config = new HashMap<>();
}
