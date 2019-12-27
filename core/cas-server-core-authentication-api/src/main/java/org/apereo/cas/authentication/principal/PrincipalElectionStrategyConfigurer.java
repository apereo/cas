package org.apereo.cas.authentication.principal;

import org.springframework.core.Ordered;

/**
 * This is {@link PrincipalElectionStrategyConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@FunctionalInterface
public interface PrincipalElectionStrategyConfigurer extends Ordered {
    void configurePrincipalElectionStrategy(ChainingPrincipalElectionStrategy chain);

    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
