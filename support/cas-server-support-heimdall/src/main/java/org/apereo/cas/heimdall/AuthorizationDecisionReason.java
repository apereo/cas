package org.apereo.cas.heimdall;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Why an authorization request was denied, returned to AuthZEN callers as the {@code reason} of the decision context.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
@RequiredArgsConstructor
public enum AuthorizationDecisionReason {
    /**
     * No principal could be resolved for the subject.
     */
    SUBJECT_UNRESOLVED("subject_unresolved"),
    /**
     * No authorizable resource matches the request.
     */
    NO_MATCHING_RESOURCE("no_matching_resource"),
    /**
     * A matching resource defines no authorization policies.
     */
    NO_POLICIES("no_policies"),
    /**
     * The policies of a matching resource denied access.
     */
    POLICY_DENIED("policy_denied");

    private final String code;
}
