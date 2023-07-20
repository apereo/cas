package org.apereo.cas.config;

import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;
import java.nio.file.Path;

/**
 * This is {@link CasConfigurationCreatedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasConfigurationCreatedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = -9038763901650896455L;

    private final transient Path file;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source the source
     * @param file   the file
     */
    public CasConfigurationCreatedEvent(final Object source, final Path file, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.file = file;
    }
}
