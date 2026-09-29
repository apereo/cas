package org.apereo.cas.heimdall.authzen;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * How an AuthZEN access evaluations request is executed.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public enum AuthZenEvaluationsSemantic {
    /**
     * Evaluate every request and return every decision.
     */
    @JsonProperty("execute_all")
    EXECUTE_ALL,
    /**
     * Stop at the first denial or error.
     */
    @JsonProperty("deny_on_first_deny")
    DENY_ON_FIRST_DENY,
    /**
     * Stop at the first permit.
     */
    @JsonProperty("permit_on_first_permit")
    PERMIT_ON_FIRST_PERMIT
}
