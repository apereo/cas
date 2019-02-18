package org.apereo.cas.support.events.authentication.adaptive;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;

/**
 * This is {@link CasRiskBasedAuthenticationEvaluationStartedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasRiskBasedAuthenticationEvaluationStartedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 748568299766263298L;

    private final Authentication authentication;
    private final RegisteredService service;

    /**
     * Instantiates a new CAS risk based authentication evaluation started event.
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
}
