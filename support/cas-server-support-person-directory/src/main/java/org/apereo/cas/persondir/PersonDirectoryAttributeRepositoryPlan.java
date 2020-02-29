package org.apereo.cas.persondir;

import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link PersonDirectoryAttributeRepositoryPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface PersonDirectoryAttributeRepositoryPlan {

    /**
     * Register attribute repository.
     *
     * @param repository the repository
     */
    void registerAttributeRepository(IPersonAttributeDao repository);

    /**
     * Register attribute repository.
     *
     * @param repository the repository
     */
    default void registerAttributeRepositories(final IPersonAttributeDao... repository) {
        Arrays.stream(repository).forEach(this::registerAttributeRepository);
    }

    /**
     * Register attribute repository.
     *
     * @param repository the repository
     */
    default void registerAttributeRepositories(final List<IPersonAttributeDao> repository) {
        repository.forEach(this::registerAttributeRepository);
    }

    /**
     * Gets attribute repositories.
     *
     * @return the attribute repositories
     */
    Collection<IPersonAttributeDao> getAttributeRepositories();
}
