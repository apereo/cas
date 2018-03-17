package org.apereo.cas.support.events.web;

import lombok.RequiredArgsConstructor;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.Collection;

/**
 * This is {@link CasEventsReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */

@RequiredArgsConstructor
@Endpoint(id = "events", enableByDefault = false)
public class CasEventsReportEndpoint {
    private final CasEventRepository eventRepository;

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
