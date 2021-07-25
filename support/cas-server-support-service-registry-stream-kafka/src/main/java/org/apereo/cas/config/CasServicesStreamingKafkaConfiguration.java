package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.kafka.KafkaObjectFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceKafkaDistributedCacheListener;
import org.apereo.cas.services.RegisteredServiceKafkaDistributedCacheManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This is {@link CasServicesStreamingKafkaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration("casServicesStreamingKafkaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.service-registry.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@EnableKafka
public class CasServicesStreamingKafkaConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casRegisteredServiceStreamPublisherIdentifier")
    private ObjectProvider<PublisherIdentifier> casRegisteredServiceStreamPublisherIdentifier;

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, DistributedCacheObject> registeredServiceKafkaListenerContainerFactory() {
        val kafka = casProperties.getServiceRegistry().getStream().getKafka();
        val factory = new KafkaObjectFactory<String, DistributedCacheObject>(kafka.getBootstrapAddress());
        factory.setConsumerGroupId("registeredServices");
        val mapper = new RegisteredServiceJsonSerializer().getObjectMapper();
        return factory.getKafkaListenerContainerFactory(new StringDeserializer(),
            new JsonDeserializer<>(DistributedCacheObject.class, mapper));
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceKafkaDistributedCacheListener")
    public RegisteredServiceKafkaDistributedCacheListener registeredServiceKafkaDistributedCacheListener() throws Exception {
        return new RegisteredServiceKafkaDistributedCacheListener(
            casRegisteredServiceStreamPublisherIdentifier.getObject(),
            registeredServiceDistributedCacheManager());
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceDistributedKafkaAdmin")
    public KafkaAdmin registeredServiceDistributedKafkaAdmin() {
        val kafka = casProperties.getServiceRegistry().getStream().getKafka();
        val factory = new KafkaObjectFactory<String, DistributedCacheObject<RegisteredService>>(kafka.getBootstrapAddress());
        return factory.getKafkaAdmin();
    }

    @Bean
    public KafkaTemplate<String, DistributedCacheObject<RegisteredService>> registeredServiceDistributedKafkaTemplate() {
        val kafka = casProperties.getServiceRegistry().getStream().getKafka();
        val mapper = new RegisteredServiceJsonSerializer().getObjectMapper();
        val factory = new KafkaObjectFactory<String, DistributedCacheObject<RegisteredService>>(kafka.getBootstrapAddress());
        return factory.getKafkaTemplate(new StringSerializer(), new JsonSerializer<>(mapper));
    }

    @Bean
    @RefreshScope
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier>
        registeredServiceDistributedCacheManager() throws Exception {

        val kafka = casProperties.getServiceRegistry().getStream().getKafka();
        val factory = new KafkaObjectFactory<String, DistributedCacheObject<RegisteredService>>(kafka.getBootstrapAddress());
        try {
            factory.getKafkaAdminClient().createTopics(List.of(registeredServiceDistributedCacheKafkaTopic())).all().get();
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                LOGGER.info(e.getMessage());
            } else {
                throw e;
            }
        }
        return new RegisteredServiceKafkaDistributedCacheManager(registeredServiceDistributedKafkaTemplate(), kafka.getTopic().getName());
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceDistributedCacheKafkaTopic")
    public NewTopic registeredServiceDistributedCacheKafkaTopic() {
        val topic = casProperties.getServiceRegistry().getStream().getKafka().getTopic();
        return TopicBuilder.name(topic.getName())
            .partitions(topic.getPartitions())
            .replicas(topic.getReplicas())
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, topic.getCompressionType())
            .compact()
            .build();
    }
}
