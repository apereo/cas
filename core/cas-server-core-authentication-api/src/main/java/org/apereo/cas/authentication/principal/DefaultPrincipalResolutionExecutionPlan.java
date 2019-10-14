package org.apereo.cas.authentication.principal;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultPrincipalResolutionExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Getter
public class DefaultPrincipalResolutionExecutionPlan implements PrincipalResolutionExecutionPlan {
    private final List<PrincipalResolver> registeredPrincipalResolvers = new ArrayList<>();

    @Override
    public void registerPrincipalResolver(final PrincipalResolver principalResolver) {
        registeredPrincipalResolvers.add(principalResolver);
        AnnotationAwareOrderComparator.sortIfNecessary(this.registeredPrincipalResolvers);
    }
}
