package org.apereo.cas.support.events.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.Collection;

/**
 * This is {@link CasEventsReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Endpoint(id = "events", enableByDefault = false)
public class CasEventsReportEndpoint extends BaseCasActuatorEndpoint {
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
    @ReadOperation
    public Collection<? extends CasEvent> events() {
        return this.eventRepository.load();
    }
}
