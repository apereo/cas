package org.apereo.cas.multitenancy;

import module java.base;

/**
 * This is {@link UnknownTenantException}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class UnknownTenantException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8915095988990722472L;

    public UnknownTenantException(final String message) {
        super(message);
    }
}
