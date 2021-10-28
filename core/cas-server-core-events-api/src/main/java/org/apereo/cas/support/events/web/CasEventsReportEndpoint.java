package org.apereo.cas.support.events.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link CasEventsReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RestControllerEndpoint(id = "events", enableByDefault = false)
public class CasEventsReportEndpoint extends BaseCasActuatorEndpoint {
    private static final long LIMIT = 1000;

    private final CasEventRepository eventRepository;

    public CasEventsReportEndpoint(final CasConfigurationProperties casProperties,
                                   final CasEventRepository eventRepository) {
        super(casProperties);
        this.eventRepository = eventRepository;
    }

    /**
     * Collect CAS events.
     *
     * @return the collection
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Provide a report of CAS events in the event repository")
    public List<? extends CasEvent> events() {
        return eventRepository.load()
            .sorted(Comparator.comparingLong(CasEvent::getTimestamp).reversed())
            .limit(LIMIT)
            .collect(Collectors.toList());
    }
}
