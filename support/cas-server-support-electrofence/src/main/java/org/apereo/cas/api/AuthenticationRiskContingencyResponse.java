package org.apereo.cas.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.webflow.execution.Event;
import lombok.ToString;

/**
 * This is {@link AuthenticationRiskContingencyResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
public class AuthenticationRiskContingencyResponse {

    private final Event result;

    public AuthenticationRiskContingencyResponse(final Event result) {
        this.result = result;
    }

    public Event getResult() {
        return result;
    }
}
