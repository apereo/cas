package org.apereo.cas.web.flow.configurer;

import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link CasMultifactorWebflowCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface CasMultifactorWebflowCustomizer extends Ordered {
    /**
     * Gets candidate states for multifactor authentication.
     *
     * @return the candidate states for multifactor authentication
     */
    default Collection<String> getCandidateStatesForMultifactorAuthentication() {
        return List.of();
    }

    /**
     * Gets multifactor webflow attribute mappings.
     *
     * @return the multifactor webflow attribute mappings
     */
    default List<String> getMultifactorWebflowAttributeMappings() {
        return List.of();
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
