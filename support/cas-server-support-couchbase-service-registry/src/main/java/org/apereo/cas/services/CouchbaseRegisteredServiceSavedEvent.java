package org.apereo.cas.services;

import org.springframework.context.ApplicationEvent;

/**
 * This is {@link CouchbaseRegisteredServiceSavedEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class CouchbaseRegisteredServiceSavedEvent extends ApplicationEvent {
    public CouchbaseRegisteredServiceSavedEvent(final Object source) {
        super(source);
    }
}
