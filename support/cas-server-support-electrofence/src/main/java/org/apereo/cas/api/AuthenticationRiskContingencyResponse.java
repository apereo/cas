package org.apereo.cas.api;

import org.springframework.webflow.execution.Event;

/**
 * This is {@link AuthenticationRiskContingencyResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationRiskContingencyResponse {
    private Event result;

    public AuthenticationRiskContingencyResponse(final Event result) {
        this.result = result;
    }

    public Event getResult() {
        return result;
    }
}
