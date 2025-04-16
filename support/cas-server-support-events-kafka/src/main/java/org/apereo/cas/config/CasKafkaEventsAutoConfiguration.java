package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.kafka.KafkaObjectFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.kafka.KafkaCasEventRepository;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdminOperations;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * This is {@link CasKafkaEventsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Events, module = "kafka")
@AutoConfiguration
public class CasKafkaEventsAutoConfiguration {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).minimal(true).build().toObjectMapper();

    @ConditionalOnMissingBean(name = "kafkaEventRepositoryFilter")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasEventRepositoryFilter kafkaEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "kafkaEventRepositoryTemplate")
    public KafkaOperations<String, CasEvent> kafkaEventRepositoryTemplate(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val kafka = casProperties.getEvents().getKafka();
        val factory = new KafkaObjectFactory<String, CasEvent>(kafka.getBootstrapAddress());
        return factory.getKafkaTemplate(new StringSerializer(), new JsonSerializer<>(MAPPER));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "kafkaEventRepositoryAdminOperations")
    public KafkaAdminOperations kafkaEventRepositoryAdminOperations(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val kafka = casProperties.getEvents().getKafka();
        val factory = new KafkaObjectFactory<String, CasEvent>(kafka.getBootstrapAddress());
        val kafkaAdmin = factory.getKafkaAdmin();
        kafkaAdmin.setAutoCreate(true);
        return kafkaAdmin;
    }

    @Bean
    @ConditionalOnMissingBean(name = "kafkaEventRepositoryTopic")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public NewTopic kafkaEventRepositoryTopic(final CasConfigurationProperties casProperties) {
        val topic = casProperties.getEvents().getKafka().getTopic();
        return TopicBuilder.name(topic.getName())
            .partitions(topic.getPartitions())
            .replicas(topic.getReplicas())
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, topic.getCompressionType())
            .compact()
            .build();
    }
    
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasEventRepository casEventRepository(
        @Qualifier("kafkaEventRepositoryFilter")
        final CasEventRepositoryFilter kafkaEventRepositoryFilter,
        @Qualifier("kafkaEventRepositoryTemplate")
        final KafkaOperations<String, CasEvent> kafkaEventRepositoryTemplate,
        final CasConfigurationProperties casProperties) {
        return new KafkaCasEventRepository(kafkaEventRepositoryFilter,
            kafkaEventRepositoryTemplate, casProperties);
    }
}
