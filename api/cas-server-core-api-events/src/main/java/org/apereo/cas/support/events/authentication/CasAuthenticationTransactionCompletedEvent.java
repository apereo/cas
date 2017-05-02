package org.apereo.cas.support.events.authentication;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.support.events.AbstractCasEvent;

/**
 * This is {@link CasAuthenticationTransactionCompletedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasAuthenticationTransactionCompletedEvent extends AbstractCasEvent {
    private static final long serialVersionUID = -1862538693590213844L;

    private final Authentication authentication;


    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source         the source
     * @param authentication the authentication
     */
    public CasAuthenticationTransactionCompletedEvent(final Object source, final Authentication authentication) {
        super(source);
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }
}
