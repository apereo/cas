package org.apereo.cas.web.flow.configurer;

import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Collection;

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
        return new ArrayList<>(0);
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
