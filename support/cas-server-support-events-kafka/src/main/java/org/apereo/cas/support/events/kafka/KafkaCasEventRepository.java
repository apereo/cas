package org.apereo.cas.support.events.kafka;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.kafka.core.KafkaOperations;

/**
 * This is {@link KafkaCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class KafkaCasEventRepository extends AbstractCasEventRepository {
    private final KafkaOperations<String, CasEvent> kafkaEventRepositoryTemplate;
    private final CasConfigurationProperties casProperties;

    public KafkaCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter,
                                   final KafkaOperations<String, CasEvent> kafkaEventRepositoryTemplate,
                                   final CasConfigurationProperties casProperties) {
        super(eventRepositoryFilter);
        this.kafkaEventRepositoryTemplate = kafkaEventRepositoryTemplate;
        this.casProperties = casProperties;
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public CasEvent saveInternal(final CasEvent event) {
        val eventToSend = event.assignIdIfNecessary();
        val topic = casProperties.getEvents().getKafka().getTopic().getName();
        val future = kafkaEventRepositoryTemplate.send(topic, eventToSend);
        future.whenComplete((result, ex) -> {
            LOGGER.trace("Published [{}]", result);
            LoggingUtils.error(LOGGER, ex);
        });
        return eventToSend;
    }
}
