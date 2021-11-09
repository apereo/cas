package org.apereo.cas.web.flow;

import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link CasWebflowLoginContextProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface CasWebflowLoginContextProvider extends Ordered {

    /**
     * Gets candidate username.
     *
     * @param context the request
     * @return the candidate username
     */
    Optional<String> getCandidateUsername(RequestContext context);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
