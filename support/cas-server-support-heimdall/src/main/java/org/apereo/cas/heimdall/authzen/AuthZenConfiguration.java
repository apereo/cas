package org.apereo.cas.heimdall.authzen;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AuthZEN policy decision point metadata.
 *
 * @param policyDecisionPoint      the policy decision point identifier
 * @param accessEvaluationEndpoint the access evaluation endpoint
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AuthZenConfiguration(
    @JsonProperty("policy_decision_point") String policyDecisionPoint,
    @JsonProperty("access_evaluation_endpoint") String accessEvaluationEndpoint) implements Serializable {
}
