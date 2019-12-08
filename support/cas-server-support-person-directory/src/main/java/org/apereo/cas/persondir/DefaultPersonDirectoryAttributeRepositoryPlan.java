package org.apereo.cas.persondir;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.aop.support.AopUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultPersonDirectoryAttributeRepositoryPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
public class DefaultPersonDirectoryAttributeRepositoryPlan implements PersonDirectoryAttributeRepositoryPlan {
    private final List<IPersonAttributeDao> attributeRepositories = new ArrayList<>(0);

    @Override
    public void registerAttributeRepository(final IPersonAttributeDao repository) {
        val name = AopUtils.isAopProxy(repository) ? AopUtils.getTargetClass(repository).getSimpleName() : repository.getClass().getSimpleName();
        LOGGER.debug("Registering attribute repository [{}] into the person directory plan", name);
        attributeRepositories.add(repository);
    }
}
