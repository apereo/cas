package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.TenantAcceptableUsagePolicyRepositoryBuilder;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link BaseAcceptableUsagePolicyAction}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
abstract class BaseAcceptableUsagePolicyAction extends BaseCasWebflowAction {
    private final AcceptableUsagePolicyRepository repository;

    @Getter
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final TenantExtractor tenantExtractor;

    protected AcceptableUsagePolicyRepository toAcceptableUsagePolicyRepository(final RequestContext requestContext) {
        val tenantDefinition = tenantExtractor.extract(requestContext).orElse(null);
        if (tenantDefinition != null && !tenantDefinition.getProperties().isEmpty()) {
            val applicationContext = requestContext.getActiveFlow().getApplicationContext();
            return applicationContext.getBeansOfType(TenantAcceptableUsagePolicyRepositoryBuilder.class)
                .values()
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .map(Unchecked.function(builder -> builder.build(tenantDefinition)))
                .filter(BeanSupplier::isNotProxy)
                .findFirst()
                .orElse(repository);
        }
        return repository;
    }
}
