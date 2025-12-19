package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.kafka.KafkaObjectFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceKafkaDistributedCacheListener;
import org.apereo.cas.services.RegisteredServiceKafkaDistributedCacheManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdminOperations;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

/**
 * This is {@link CasServicesStreamingKafkaAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@EnableKafka
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistryStreaming, module = "kafka")
@AutoConfiguration
@Lazy(false)
public class CasServicesStreamingKafkaAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.service-registry.stream.core.enabled").isTrue().evenIfMissing();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "registeredServiceKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull DistributedCacheObject> registeredServiceKafkaListenerContainerFactory(
        @Qualifier("casRegisteredServiceStreamPublisherIdentifier")
        final PublisherIdentifier casRegisteredServiceStreamPublisherIdentifier,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val kafka = casProperties.getServiceRegistry().getStream().getKafka();
        val factory = new KafkaObjectFactory<String, DistributedCacheObject>(kafka.getBootstrapAddress());
        factory.setConsumerGroupId(casRegisteredServiceStreamPublisherIdentifier.getId());
        val mapper = new RegisteredServiceJsonSerializer(applicationContext).getJsonMapper();
        return factory.getKafkaListenerContainerFactory(new StringDeserializer(),
            new JacksonJsonDeserializer<>(DistributedCacheObject.class, mapper));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "registeredServiceKafkaDistributedCacheListener")
    public RegisteredServiceKafkaDistributedCacheListener registeredServiceKafkaDistributedCacheListener(
        @Qualifier("registeredServiceDistributedCacheManager")
        final DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager,
        @Qualifier("casRegisteredServiceStreamPublisherIdentifier")
        final PublisherIdentifier casRegisteredServiceStreamPublisherIdentifier) {
        return new RegisteredServiceKafkaDistributedCacheListener(
            casRegisteredServiceStreamPublisherIdentifier, registeredServiceDistributedCacheManager);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "registeredServiceDistributedKafkaAdmin")
    public KafkaAdminOperations registeredServiceDistributedKafkaAdmin(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(KafkaAdminOperations.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val kafka = casProperties.getServiceRegistry().getStream().getKafka();
                val factory = new KafkaObjectFactory<String, DistributedCacheObject<RegisteredService>>(kafka.getBootstrapAddress());
                return factory.getKafkaAdmin();
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public KafkaOperations<@NonNull String, @NonNull DistributedCacheObject<RegisteredService>> registeredServiceDistributedKafkaTemplate(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(KafkaOperations.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val kafka = casProperties.getServiceRegistry().getStream().getKafka();
                val mapper = new RegisteredServiceJsonSerializer(applicationContext).getJsonMapper();
                val factory = new KafkaObjectFactory<String, DistributedCacheObject<RegisteredService>>(kafka.getBootstrapAddress());
                return factory.getKafkaTemplate(new StringSerializer(), new JacksonJsonSerializer<>(mapper));
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("registeredServiceDistributedCacheKafkaTopic")
        final NewTopic registeredServiceDistributedCacheKafkaTopic,
        @Qualifier("registeredServiceDistributedKafkaTemplate")
        final KafkaOperations<@NonNull String, @NonNull DistributedCacheObject<RegisteredService>> registeredServiceDistributedKafkaTemplate) {
        return BeanSupplier.of(DistributedCacheManager.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val kafka = casProperties.getServiceRegistry().getStream().getKafka();
                val factory = new KafkaObjectFactory<String, DistributedCacheObject<RegisteredService>>(kafka.getBootstrapAddress());
                try {
                    factory.getKafkaAdminClient().createTopics(List.of(registeredServiceDistributedCacheKafkaTopic)).all().get();
                } catch (final ExecutionException e) {
                    if (e.getCause() instanceof TopicExistsException) {
                        LOGGER.info(e.getMessage());
                    } else {
                        throw e;
                    }
                }
                return new RegisteredServiceKafkaDistributedCacheManager(registeredServiceDistributedKafkaTemplate, kafka.getTopic().getName());
            }))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceDistributedCacheKafkaTopic")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public NewTopic registeredServiceDistributedCacheKafkaTopic(final CasConfigurationProperties casProperties) {
        val topic = casProperties.getServiceRegistry().getStream().getKafka().getTopic();
        return TopicBuilder.name(topic.getName())
            .partitions(topic.getPartitions())
            .replicas(topic.getReplicas())
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, topic.getCompressionType())
            .compact()
            .build();
    }
}
