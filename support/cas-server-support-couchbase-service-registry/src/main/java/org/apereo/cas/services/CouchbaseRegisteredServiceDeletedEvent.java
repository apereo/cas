package org.apereo.cas.services;

import org.springframework.context.ApplicationEvent;

/**
 * This is {@link CouchbaseRegisteredServiceDeletedEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class CouchbaseRegisteredServiceDeletedEvent extends ApplicationEvent {
    public CouchbaseRegisteredServiceDeletedEvent(final Object source) {
        super(source);
    }
}
