package org.apereo.cas.persondir;

import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.Collection;

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
     * Gets attribute repositories.
     *
     * @return the attribute repositories
     */
    Collection<IPersonAttributeDao> getAttributeRepositories();
}
