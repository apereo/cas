package org.apereo.cas.persondir;

import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultPersonDirectoryAttributeRepositoryPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultPersonDirectoryAttributeRepositoryPlan implements PersonDirectoryAttributeRepositoryPlan {
    private List<IPersonAttributeDao> attributeRepositories = new ArrayList<>();

    @Override
    public void registerAttributeRepository(final IPersonAttributeDao repository) {
        attributeRepositories.add(repository);
    }

    @Override
    public Collection<IPersonAttributeDao> getAttributeRepositories() {
        return attributeRepositories;
    }
}
