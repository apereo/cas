package org.apereo.cas.support.events.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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

    public Path getFile() {
        return file;
    }
}
