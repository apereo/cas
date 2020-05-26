package org.apereo.cas.kafka;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;

/**
 * This is {@link KafkaObjectFactory}.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class KafkaObjectFactory<K, V> {
    private final String bootstrapAddress;

    /**
     * Gets producer factory.
     *
     * @return the producer factory
     */
    private ProducerFactory<K, V> getProducerFactory() {
        val configProps = new HashMap<String, Object>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.RETRIES_CONFIG, 0);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Gets kafka admin.
     *
     * @return the kafka admin
     */
    public KafkaAdmin getKafkaAdmin() {
        val configs = new HashMap<String, Object>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    /**
     * Gets kafka template.
     *
     * @return the kafka template
     */
    public KafkaTemplate<K, V> getKafkaTemplate() {
        return new KafkaTemplate<>(getProducerFactory());
    }
}
