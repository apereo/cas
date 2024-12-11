package org.apereo.cas.support.events.ticket;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.ToString;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;

/**
 * Concrete subclass of {@link AbstractCasEvent} representing validation error of a
 * service ticket by a CAS server.
 *
 * @author Brian Kerr
 * @since 7.2.0
 */
@ToString(callSuper = true)
@Getter
public class CasServiceTicketValidateErrorEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = 3285486733924517014L;

    private final String code;

    private final String description;

    private final HttpServletRequest request;

    private final WebApplicationService service;

    /**
     * Instantiates a new CAS service ticket validate error event.
     *
     * @param source        the source
     * @param code          the code
     * @param description   the description
     * @param request       the request
     * @param service       the service
     * @param clientInfo    the client info
     */
    public CasServiceTicketValidateErrorEvent(final Object source, final String code, final String description,
                                              final HttpServletRequest request, final WebApplicationService service,
                                              final ClientInfo clientInfo) {
        super(source, clientInfo);

        this.code = code;
        this.description = description;
        this.request = request;
        this.service = service;
    }
}
