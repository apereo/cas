package org.apereo.cas.api;

import module java.base;
import org.springframework.webflow.execution.Event;

/**
 * This is {@link AuthenticationRiskContingencyResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public record AuthenticationRiskContingencyResponse(Event result) {
}
