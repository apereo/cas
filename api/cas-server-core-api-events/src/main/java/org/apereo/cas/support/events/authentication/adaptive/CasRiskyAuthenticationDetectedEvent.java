package org.apereo.cas.support.events.authentication.adaptive;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.AbstractCasEvent;

/**
 * This is {@link CasRiskyAuthenticationDetectedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasRiskyAuthenticationDetectedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 291168297497263298L;

    private final Authentication authentication;
    private final RegisteredService service;
    private final Object score;

    /**
     * Instantiates a new Cas risky authentication detected event.
     *
     * @param source         the source
     * @param authentication the authentication
     * @param service        the service
     * @param riskScore      the risk score
     */
    public CasRiskyAuthenticationDetectedEvent(final Object source, final Authentication authentication, 
                                               final RegisteredService service, 
                                               final Object riskScore) {
        super(source);
        this.authentication = authentication;
        this.service = service;
        this.score = riskScore;
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
