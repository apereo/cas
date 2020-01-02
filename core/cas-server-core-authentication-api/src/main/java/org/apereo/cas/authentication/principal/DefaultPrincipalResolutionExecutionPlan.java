package org.apereo.cas.authentication.principal;

import lombok.Getter;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultPrincipalResolutionExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
public class DefaultPrincipalResolutionExecutionPlan implements PrincipalResolutionExecutionPlan {
    private final List<PrincipalResolver> registeredPrincipalResolvers = new ArrayList<>(0);

    @Override
    public void registerPrincipalResolver(final PrincipalResolver principalResolver) {
        registeredPrincipalResolvers.add(principalResolver);
        AnnotationAwareOrderComparator.sortIfNecessary(this.registeredPrincipalResolvers);
    }
}
