package org.apereo.cas.heimdall.authzen;

import module java.base;

/**
 * AuthZEN access evaluations response, with decisions in the order of the requested evaluations.
 *
 * @author Misagh Moayyed
 * @param evaluations the decisions
 * @since 8.1.0
 */
public record AuthZenEvaluationsResponse(List<AuthZenResponse> evaluations) implements Serializable {
}
