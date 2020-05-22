package org.apereo.cas.persondir;

import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
     * Find attribute repositories and return the stream.
     *
     * @param filter the filter
     * @return the stream
     */
    default Stream<IPersonAttributeDao> findAttributeRepositories(Predicate<IPersonAttributeDao> filter) {
        return getAttributeRepositories().stream().filter(filter);
    }

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
     * Indicate whether any attribute repositories are currently registered with the plan.
     *
     * @return true/false
     */
    default boolean isEmpty() {
        return getAttributeRepositories().isEmpty();
    }

    /**
     * Gets attribute repositories.
     *
     * @return the attribute repositories
     */
    List<IPersonAttributeDao> getAttributeRepositories();
}
