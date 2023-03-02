package org.apereo.cas.services;

import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * This is {@link CouchbaseRegisteredServiceSavedEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 * @deprecated Since 7.0.0
 */
@Deprecated(since = "7.0.0")
public class CouchbaseRegisteredServiceSavedEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = 5538958334155906185L;

    public CouchbaseRegisteredServiceSavedEvent(final Object source) {
        super(source);
    }
}
