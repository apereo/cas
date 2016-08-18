package org.apereo.cas.support.events;

import org.apereo.cas.authentication.Credential;

/**
 * This is {@link CasAuthenticationTransactionStartedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasAuthenticationTransactionStartedEvent extends AbstractCasEvent {
    private static final long serialVersionUID = -1862937393590213811L;

    private Credential credential;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source the source
     * @param c      the c
     */
    public CasAuthenticationTransactionStartedEvent(final Object source, final Credential c) {
        super(source);
        this.credential = c;
    }

    public Credential getCredential() {
        return credential;
    }
}
