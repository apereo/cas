package org.apereo.cas.support.events.authentication;

import org.apereo.cas.authentication.Authentication;

import java.util.Map;

/**
 * This is {@link CasAuthenticationPolicyFailureEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasAuthenticationPolicyFailureEvent extends CasAuthenticationTransactionFailureEvent {
    private static final long serialVersionUID = 2208076621158767073L;
    private final Authentication authentication;

    public CasAuthenticationPolicyFailureEvent(final Object source, final Map<String, Class<? extends Exception>> failures,
                                               final Authentication authentication) {
        super(source, failures);
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }
}
