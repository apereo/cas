package org.apereo.cas.support.events.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link CasEventsReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Endpoint(id = "events", enableByDefault = false)
public class CasEventsReportEndpoint extends BaseCasActuatorEndpoint {
    private static final long LIMIT = 1000;

    private final ApplicationContext applicationContext;

    public CasEventsReportEndpoint(final CasConfigurationProperties casProperties,
                                   final ApplicationContext applicationContext) {
        super(casProperties);
        this.applicationContext = applicationContext;
    }

    /**
     * Collect CAS events.
     *
     * @return the collection
     */
    @ReadOperation
    @Operation(summary = "Provide a report of CAS events in the event repository")
    public List<? extends CasEvent> events() {
        val eventRepository = applicationContext.getBean(CasEventRepository.BEAN_NAME, CasEventRepository.class);
        return eventRepository.load()
            .sorted(Comparator.comparingLong(CasEvent::getTimestamp).reversed())
            .limit(LIMIT)
            .collect(Collectors.toList());
    }
}
