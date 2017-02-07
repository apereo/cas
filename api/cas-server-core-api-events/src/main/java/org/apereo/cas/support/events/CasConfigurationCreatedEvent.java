package org.apereo.cas.support.events;

import java.nio.file.Path;

/**
 * This is {@link CasConfigurationCreatedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasConfigurationCreatedEvent extends AbstractCasEvent {
    private static final long serialVersionUID = -9038763901650896455L;

    private final Path file;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source the source
     */
    public CasConfigurationCreatedEvent(final Object source, final Path file) {
        super(source);
        this.file = file;
    }
}
