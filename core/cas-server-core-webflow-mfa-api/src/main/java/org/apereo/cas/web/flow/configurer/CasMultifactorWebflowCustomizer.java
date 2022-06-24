package org.apereo.cas.web.flow.configurer;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link CasMultifactorWebflowCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface CasMultifactorWebflowCustomizer extends CasWebflowCustomizer {
    /**
     * Gets candidate states for multifactor authentication.
     *
     * @return the candidate states for multifactor authentication
     */
    default Collection<String> getCandidateStatesForMultifactorAuthentication() {
        return List.of();
    }
}
