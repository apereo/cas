package org.apereo.cas.persondir;

import module java.base;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;

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
    private final List<PersonAttributeDao> attributeRepositories = new ArrayList<>();

    private final List<PersonDirectoryAttributeRepositoryCustomizer> attributeRepositoryCustomizers;

    @Override
    public void registerAttributeRepository(final PersonAttributeDao repository) {
        if (BeanSupplier.isNotProxy(repository)) {
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
    }

    @Override
    public void destroy() {
        LOGGER.trace("Closing attribute repositories, if any");
        findAttributeRepositories(AutoCloseable.class::isInstance)
            .map(AutoCloseable.class::cast)
            .forEach(Unchecked.consumer(AutoCloseable::close));
    }
}
