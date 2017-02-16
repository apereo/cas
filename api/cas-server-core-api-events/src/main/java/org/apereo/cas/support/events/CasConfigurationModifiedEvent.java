package org.apereo.cas.support.events;

import java.nio.file.Path;

/**
 * This is {@link CasConfigurationModifiedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasConfigurationModifiedEvent extends AbstractCasEvent {
    private static final long serialVersionUID = -5738763037210896455L;

    private final Path file;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source the source
     */
    public CasConfigurationModifiedEvent(final Object source, final Path file) {
        super(source);
        this.file = file;
    }

    public Path getFile() {
        return file;
    }
}
