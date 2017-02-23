package org.apereo.cas.support.events.authentication.adaptive;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.AbstractCasEvent;

/**
 * This is {@link CasRiskyAuthenticationMitigatedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasRiskyAuthenticationMitigatedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 291198069766263578L;

    private final Authentication authentication;
    private final RegisteredService service;
    private final Object response;

    /**
     * Instantiates a new Cas risky authentication mitigated event.
     *
     * @param source         the source
     * @param authentication the authentication
     * @param service        the service
     * @param response       the response
     */
    public CasRiskyAuthenticationMitigatedEvent(final Object source, final Authentication authentication, 
                                                final RegisteredService service, final Object response) {
        super(source);
        this.authentication = authentication;
        this.service = service;
        this.response = response;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public RegisteredService getService() {
        return service;
    }

    public Object getResponse() {
        return response;
    }
}
