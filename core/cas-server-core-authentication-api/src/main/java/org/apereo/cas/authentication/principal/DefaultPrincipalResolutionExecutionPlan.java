package org.apereo.cas.authentication.principal;

import module java.base;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * This is {@link DefaultPrincipalResolutionExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
public class DefaultPrincipalResolutionExecutionPlan implements PrincipalResolutionExecutionPlan {
    private final List<PrincipalResolver> registeredPrincipalResolvers = new ArrayList<>();

    @Override
    public void registerPrincipalResolver(final PrincipalResolver principalResolver) {
        registeredPrincipalResolvers.add(principalResolver);
        AnnotationAwareOrderComparator.sortIfNecessary(this.registeredPrincipalResolvers);
    }
}
