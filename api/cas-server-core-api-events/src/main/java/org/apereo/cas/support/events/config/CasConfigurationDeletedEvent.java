package org.apereo.cas.support.events.config;

import module java.base;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

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

    public CasConfigurationDeletedEvent(final Object source, final Path file, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.file = file;
    }
}
