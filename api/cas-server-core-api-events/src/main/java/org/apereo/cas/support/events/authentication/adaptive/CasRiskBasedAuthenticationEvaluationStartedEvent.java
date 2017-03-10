package org.apereo.cas.support.events.authentication.adaptive;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.AbstractCasEvent;

/**
 * This is {@link CasRiskBasedAuthenticationEvaluationStartedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasRiskBasedAuthenticationEvaluationStartedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 748568299766263298L;

    private final Authentication authentication;
    private final RegisteredService service;

    /**
     * Instantiates a new Cas risk based authentication evaluation started event.
     *
     * @param source         the source
     * @param authentication the authentication
     * @param service        the service
     */
    public CasRiskBasedAuthenticationEvaluationStartedEvent(final Object source,
                                                            final Authentication authentication,
                                                            final RegisteredService service) {
        super(source);
        this.authentication = authentication;
        this.service = service;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public RegisteredService getService() {
        return service;
    }
}
