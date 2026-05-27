package org.apereo.cas.consent;

import module java.base;

/**
 * This is {@link ConsentRepositoryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@FunctionalInterface
public interface ConsentRepositoryBuilder {
    /**
     * Register consent repository.
     */
    ConsentRepository createConsentRepository();
}
