package org.apereo.cas.audit;

import org.apereo.cas.authentication.Authentication;

import org.springframework.core.Ordered;

/**
 * Strategy interface to provide principal id tokens from any given authentication event.
 * <p>
 * Useful for authentication scenarios where there is not only one primary principal id available, but additional authentication metadata
 * in addition to custom requirement to compute and show more complex principal identifier for auditing purposes.
 * An example would be compound ids resulted from multi-legged mfa authentications, 'surrogate' authentications, etc.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
public interface AuditPrincipalIdProvider extends Ordered {

    /**
     * Return principal id from a given authentication event.
     *
     * @param authentication authentication event containing the data to computed the final principal id from
     * @param resultValue    the result value that is currently processed by the executing op. May be null.
     * @param exception      the exception that may have occurred as part of the current executing op. May be null.
     * @return computed principal id
     */
    String getPrincipalIdFrom(Authentication authentication, Object resultValue, Exception exception);

    /**
     * Whether this provider can support the authentication transaction to provide a principal id.
     *
     * @param authentication the authentication transaction.
     * @param resultValue    the result value that is currently processed by the executing op. May be null.
     * @param exception      the exception that may have occurred as part of the current executing op. May be null.
     * @return true /false
     */
    boolean supports(Authentication authentication, Object resultValue, Exception exception);
}
