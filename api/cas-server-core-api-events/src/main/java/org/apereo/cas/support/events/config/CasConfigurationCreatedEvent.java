package org.apereo.cas.support.events.config;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.events.AbstractCasEvent;

import java.nio.file.Path;

/**
 * This is {@link CasConfigurationCreatedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
public class CasConfigurationCreatedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = -9038763901650896455L;

    private final transient Path file;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source the source
     * @param file   the file
     */
    public CasConfigurationCreatedEvent(final Object source, final Path file) {
        super(source);
        this.file = file;
    }
}
