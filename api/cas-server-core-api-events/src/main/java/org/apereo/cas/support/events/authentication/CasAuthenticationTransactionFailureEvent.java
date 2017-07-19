package org.apereo.cas.support.events.authentication;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.support.events.AbstractCasEvent;

import java.util.Collection;
import java.util.Map;

/**
 * This is {@link CasAuthenticationTransactionFailureEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasAuthenticationTransactionFailureEvent extends AbstractCasEvent {
    private static final long serialVersionUID = 8059647975948452375L;

    private final Map<String, Class<? extends Throwable>> failures;
    private final Collection<Credential> credential;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source     the source
     * @param failures   the failures
     * @param credential the credential
     */
    public CasAuthenticationTransactionFailureEvent(final Object source, 
                                                    final Map<String, Class<? extends Throwable>> failures,
                                                    final Collection<Credential> credential) {
        super(source);
        this.failures = failures;
        this.credential= credential;
    }

    public Credential getCredential() {
        return credential.iterator().next();
    }

    public Map<String, Class<? extends Throwable>> getFailures() {
        return failures;
    }
}
