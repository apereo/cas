package org.apereo.cas.support.events.authentication.adaptive;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.AbstractCasEvent;

/**
 * This is {@link CasRiskBasedAuthenticationMitigationStartedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasRiskBasedAuthenticationMitigationStartedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 123568299766263298L;

    private final Authentication authentication;
    private final RegisteredService service;
    private final Object score;

    /**
     * Instantiates a new Cas risk based authentication mitigation started event.
     *
     * @param source         the source
     * @param authentication the authentication
     * @param service        the service
     * @param score          the score
     */
    public CasRiskBasedAuthenticationMitigationStartedEvent(final Object source, final Authentication authentication, 
                                                            final RegisteredService service, final Object score) {
        super(source);
        this.authentication = authentication;
        this.service = service;
        this.score = score;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public RegisteredService getService() {
        return service;
    }

    public Object getScore() {
        return score;
    }
}
