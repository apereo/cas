package org.apereo.cas.ticket;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.ContextualAuthenticationPolicy;

/**
 * Error condition arising at ticket creation or validation time when a ticketing operation relying on authentication
 * cannot proceed due to unsatisfied authentication security policy.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Getter
public class UnsatisfiedAuthenticationPolicyException extends AbstractTicketException {

    /**
     * Serializable ID for unique id.
     */
    private static final long serialVersionUID = -827432780367197133L;

    /**
     * Code description.
     */
    private static final String CODE = "UNSATISFIED_AUTHN_POLICY";

    /**
     * Unfulfilled policy that caused this exception.
     */
    private final ContextualAuthenticationPolicy<?> policy;

    /**
     * Creates a new instance with no cause.
     *
     * @param policy Non-null unfulfilled security policy that caused exception.
     */
    public UnsatisfiedAuthenticationPolicyException(@NonNull final ContextualAuthenticationPolicy<?> policy) {
        super(policy.getCode().orElse(CODE));
        this.policy = policy;
    }

}
