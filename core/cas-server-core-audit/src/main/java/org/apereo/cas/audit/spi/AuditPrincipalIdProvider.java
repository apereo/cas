package org.apereo.cas.audit.spi;

import org.apereo.cas.authentication.Authentication;

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
public interface AuditPrincipalIdProvider {

    /**
     * Return principal id from a given authentication event.
     *
     * @param authentication authentication event containing the data to computed the final principal id from
     * @return computed principal id
     */
    default String getPrincipalIdFrom(final Authentication authentication) {
        return authentication != null ? authentication.getPrincipal().getId() : null;
    }
}
