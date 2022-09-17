package org.apereo.cas.services;

import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * This is {@link CouchbaseRegisteredServiceDeletedEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class CouchbaseRegisteredServiceDeletedEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = -6926761736041237960L;

    public CouchbaseRegisteredServiceDeletedEvent(final Object source) {
        super(source);
    }
}
