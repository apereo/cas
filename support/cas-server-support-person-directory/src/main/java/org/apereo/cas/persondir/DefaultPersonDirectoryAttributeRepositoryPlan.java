package org.apereo.cas.persondir;

import lombok.extern.slf4j.Slf4j;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.aop.support.AopUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultPersonDirectoryAttributeRepositoryPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DefaultPersonDirectoryAttributeRepositoryPlan implements PersonDirectoryAttributeRepositoryPlan {
    private List<IPersonAttributeDao> attributeRepositories = new ArrayList<>();

    @Override
    public void registerAttributeRepository(final IPersonAttributeDao repository) {
        final var name = AopUtils.isAopProxy(repository) ? AopUtils.getTargetClass(repository).getSimpleName() : repository.getClass().getSimpleName();
        LOGGER.debug("Registering attribute repository [{}] into the person directory plan", name);
        attributeRepositories.add(repository);
    }

    @Override
    public Collection<IPersonAttributeDao> getAttributeRepositories() {
        return attributeRepositories;
    }
}
