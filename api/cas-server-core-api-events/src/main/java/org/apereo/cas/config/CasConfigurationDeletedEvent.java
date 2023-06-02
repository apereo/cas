package org.apereo.cas.config;

import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -5738769364210896455L;

    private final transient Path file;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source the source
     * @param file   the file
     */
    public CasConfigurationDeletedEvent(final Object source, final Path file, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.file = file;
    }
}
