package org.apereo.cas.support.events.authentication;

import org.apereo.cas.support.events.AbstractCasEvent;

import java.util.Map;

/**
 * This is {@link CasAuthenticationTransactionFailureEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasAuthenticationTransactionFailureEvent extends AbstractCasEvent {
    private static final long serialVersionUID = 8059647975948452375L;

    private Map<String, Class<? extends Exception>> failures;


    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source   the source
     * @param failures the failures
     */
    public CasAuthenticationTransactionFailureEvent(final Object source, final Map<String, Class<? extends Exception>> failures) {
        super(source);
        this.failures = failures;
    }

    public Map<String, Class<? extends Exception>> getFailures() {
        return failures;
    }
}
