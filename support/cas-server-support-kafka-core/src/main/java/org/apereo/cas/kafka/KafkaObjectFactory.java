package org.apereo.cas.kafka;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link KafkaObjectFactory}.
 *
 * @author Misagh Moayyed
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Setter
public class KafkaObjectFactory<K, V> {
    private final String bootstrapAddress;


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
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.consumerGroupId);
        return props;
    }

    /**
     * Gets kafka listener container factory.
     *
     * @param keyDeserializer   the key deserializer
     * @param valueDeserializer the value deserializer
     * @return the kafka listener container factory
     */
    public ConcurrentKafkaListenerContainerFactory<K, V> getKafkaListenerContainerFactory(final Deserializer<K> keyDeserializer,
                                                                                          final Deserializer<V> valueDeserializer) {
        val listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<K, V>();
        val consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerConfiguration(), keyDeserializer, valueDeserializer);
        listenerContainerFactory.setConsumerFactory(consumerFactory);
        return listenerContainerFactory;
    }

    /**
     * Gets kafka template.
     *
     * @param keySerializer   the key serializer
     * @param valueSerializer the value serializer
     * @return the kafka template
     */
    public KafkaTemplate<K, V> getKafkaTemplate(final Serializer<K> keySerializer,
                                                final Serializer<V> valueSerializer) {
        val producerFactory = new DefaultKafkaProducerFactory<>(getProducerConfiguration(), keySerializer, valueSerializer);
        return new KafkaTemplate<>(producerFactory);
    }
}
