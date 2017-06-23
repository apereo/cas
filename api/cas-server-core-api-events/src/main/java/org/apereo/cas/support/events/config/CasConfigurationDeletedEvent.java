package org.apereo.cas.support.events.config;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.cas.support.events.AbstractCasEvent;

import java.nio.file.Path;

/**
 * This is {@link CasConfigurationDeletedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
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

    public Path getFile() {
        return file;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .appendSuper(super.toString())
                .append("file", file)
                .toString();
    }
}
