package org.apereo.cas.services.publisher;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.StringBean;
import org.apereo.cas.services.RegisteredService;
import org.springframework.context.ApplicationEvent;

import java.io.Serializable;

/**
 * This is {@link RegisteredServicesQueuedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RegisteredServicesQueuedEvent implements Serializable {
    private static final long serialVersionUID = -8826414612954655099L;
    private final String timestamp;
    private final ApplicationEvent event;
    private final RegisteredService service;
    private final StringBean publisher;

    public RegisteredServicesQueuedEvent(final String timestamp, final ApplicationEvent event,
                                         final RegisteredService service, final StringBean publisher) {
        this.timestamp = timestamp;
        this.event = event;
        this.service = service;
        this.publisher = publisher;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ApplicationEvent getEvent() {
        return event;
    }

    public RegisteredService getService() {
        return service;
    }

    public StringBean getPublisher() {
        return publisher;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("timestamp", timestamp)
                .append("event", event.getClass().getSimpleName())
                .append("service", service.getServiceId())
                .append("publisher", publisher.getId())
                .toString();
    }
}
