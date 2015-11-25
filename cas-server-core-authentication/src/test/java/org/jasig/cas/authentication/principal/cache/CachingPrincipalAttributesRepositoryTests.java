package org.jasig.cas.authentication.principal.cache;

import java.util.concurrent.TimeUnit;

/**
 * Handles tests for {@link CachingPrincipalAttributesRepository}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class CachingPrincipalAttributesRepositoryTests extends AbstractCachingPrincipalAttributesRepositoryTests {

    @Override
    protected AbstractPrincipalAttributesRepository getPrincipalAttributesRepository(final TimeUnit unit, final long duration) {
        final CachingPrincipalAttributesRepository repo = new CachingPrincipalAttributesRepository(unit, duration);
        repo.setAttributeRepository(this.dao);
        return repo;
    }
}
