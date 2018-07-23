package org.apereo.cas.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.webflow.execution.Event;

/**
 * This is {@link AuthenticationRiskContingencyResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@RequiredArgsConstructor
@Getter
public class AuthenticationRiskContingencyResponse {
    private final Event result;
}
