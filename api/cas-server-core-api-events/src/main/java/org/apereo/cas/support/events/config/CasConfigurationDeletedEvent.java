package org.apereo.cas.support.events.config;

import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;

/**
 * This is {@link CasConfigurationDeletedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasConfigurationDeletedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = -5738769364210896455L;

    private final transient Path file;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source the source
     * @param file   the file
     */
    public CasConfigurationDeletedEvent(final Object source, final Path file) {
        super(source);
        this.file = file;
    }
}
