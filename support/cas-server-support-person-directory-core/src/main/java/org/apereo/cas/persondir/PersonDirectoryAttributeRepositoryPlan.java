package org.apereo.cas.persondir;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
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
     * The bean name.
     */
    String BEAN_NAME = "personDirectoryAttributeRepositoryPlan";

    /**
     * Register attribute repository.
     *
     * @param repository the repository
     */
    void registerAttributeRepository(PersonAttributeDao repository);

    /**
     * Find attribute repositories and return the stream.
     *
     * @param filter the filter
     * @return the stream
     */
    default Stream<PersonAttributeDao> findAttributeRepositories(final Predicate<PersonAttributeDao> filter) {
        return getAttributeRepositories().stream().filter(filter);
    }

    /**
     * Register attribute repository.
     *
     * @param repository the repository
     */
    default void registerAttributeRepositories(final PersonAttributeDao... repository) {
        Arrays.stream(repository).forEach(this::registerAttributeRepository);
    }

    /**
     * Register attribute repository.
     *
     * @param repository the repository
     */
    default void registerAttributeRepositories(final List<PersonAttributeDao> repository) {
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
    List<PersonAttributeDao> getAttributeRepositories();
}
