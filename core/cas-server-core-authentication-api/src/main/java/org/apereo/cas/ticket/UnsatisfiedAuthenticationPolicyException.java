package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.authentication.AuthenticationPolicy;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * Error condition arising at ticket creation or validation time when a ticketing operation relying on authentication
 * cannot proceed due to unsatisfied authentication security policy.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
public class UnsatisfiedAuthenticationPolicyException extends AbstractTicketException {

    @Serial
    private static final long serialVersionUID = -827432780367197133L;

    /**
     * Code description.
     */
    private static final String CODE = "UNSATISFIED_AUTHN_POLICY";

    /**
     * Unfulfilled policy that caused this exception.
     */
    private final AuthenticationPolicy policy;

    public UnsatisfiedAuthenticationPolicyException(final @NonNull AuthenticationPolicy policy) {
        super(CODE);
        this.policy = policy;
    }

}
