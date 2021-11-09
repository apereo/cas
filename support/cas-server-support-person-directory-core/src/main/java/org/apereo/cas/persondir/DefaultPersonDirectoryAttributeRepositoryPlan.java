package org.apereo.cas.persondir;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.jooq.lambda.Unchecked;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This is {@link DefaultPersonDirectoryAttributeRepositoryPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class DefaultPersonDirectoryAttributeRepositoryPlan implements PersonDirectoryAttributeRepositoryPlan, DisposableBean {
    private final List<IPersonAttributeDao> attributeRepositories = new ArrayList<>(0);

    private final List<PersonDirectoryAttributeRepositoryCustomizer> attributeRepositoryCustomizers;

    @Override
    public void registerAttributeRepository(final IPersonAttributeDao repository) {
        if (LOGGER.isTraceEnabled()) {
            val name = AopUtils.isAopProxy(repository)
                ? AopUtils.getTargetClass(repository).getSimpleName()
                : repository.getClass().getSimpleName();
            LOGGER.trace("Registering attribute repository [{}] into the person directory plan", name);
        }
        attributeRepositoryCustomizers.stream()
            .sorted(Comparator.comparing(PersonDirectoryAttributeRepositoryCustomizer::getOrder))
            .filter(cust -> cust.supports(repository))
            .forEach(cust -> cust.customize(repository));
        attributeRepositories.add(repository);
    }

    @Override
    public void destroy() {
        LOGGER.trace("Closing attribute repositories, if any");
        findAttributeRepositories(repo -> repo instanceof AutoCloseable)
            .map(AutoCloseable.class::cast)
            .forEach(Unchecked.consumer(AutoCloseable::close));
    }
}
