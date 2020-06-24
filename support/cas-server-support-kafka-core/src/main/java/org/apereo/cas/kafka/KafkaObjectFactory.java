package org.apereo.cas.kafka;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link KafkaObjectFactory}.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Setter
public class KafkaObjectFactory<K, V> {
    private final String bootstrapAddress;

    private Class keySerializerClass = StringSerializer.class;

    private Class valueSerializerClass = StringSerializer.class;

    private Class keyDeserializerClass = StringDeserializer.class;

    private Class valueDeserializerClass = StringDeserializer.class;

    private String consumerGroupId;

    /**
     * Gets kafka admin.
     *
     * @return the kafka admin
     */
    public KafkaAdmin getKafkaAdmin() {
        val configs = new HashMap<String, Object>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        val admin = new KafkaAdmin(configs);
        admin.setFatalIfBrokerNotAvailable(true);
        return admin;
    }

    /**
     * Gets kafka admin client.
     *
     * @return the kafka admin client
     */
    public AdminClient getKafkaAdminClient() {
        return AdminClient.create(getKafkaAdmin().getConfigurationProperties());
    }

    /**
     * Gets producer configuration.
     *
     * @return the producer configuration
     */
    public Map<String, Object> getProducerConfiguration() {
        val configProps = new HashMap<String, Object>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.RETRIES_CONFIG, 1);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, this.keySerializerClass);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, this.valueSerializerClass);
        return configProps;
    }

    /**
     * Gets consumer configuration.
     *
     * @return the consumer configuration
     */
    public Map<String, Object> getConsumerConfiguration() {
        val props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, this.keyDeserializerClass);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.consumerGroupId);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, this.valueDeserializerClass);
        return props;
    }
}
