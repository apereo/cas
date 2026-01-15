package org.apereo.cas.support.events.ticket;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * Concrete subclass of {@link AbstractCasEvent} representing validation error of a
 * service ticket by a CAS server.
 *
 * @author Brian Kerr
 * @since 7.2.0
 */
@ToString(callSuper = true)
@Getter
public class CasServiceTicketValidationFailedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = 3285486733924517014L;

    private final String code;

    private final String description;

    private final WebApplicationService service;

    public CasServiceTicketValidationFailedEvent(final Object source, final String code, final String description,
                                                 final WebApplicationService service, final ClientInfo clientInfo) {
        super(source, clientInfo);

        this.code = code;
        this.description = description;
        this.service = service;
    }
}
